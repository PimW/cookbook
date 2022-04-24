(ns cookbook.cooklang.parser.parser
  "Cooklang parsing

  The cooklang file format is specified by: https://cooklang.org/docs/spec/

  The internal representation of a cooklang recipe is
  of the format:
  > Recipe
    > metadata {}
       > key: value
    > steps []
       > step []
          > text
            - text
          > comment
            - comment
          > ingredient
            - ingredient
            - amount
            - unit
          > cookware
            - cookware
          > timer
            - name
            - amount
            - unit"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cookbook.cooklang.parser.regex :as re]
            [clojure.spec.alpha :as s]))



(defn- extract-metadata
  "Extract a line of metadata of the form:
  >> <key>: <value>"
  [line]
  (let [res (re-find re/metadata line)]
    [(second res) (nth res 2 nil)]))

(defn- next-special-char
  "Return the index of the next special character [-@#~]
  in a string. Ignores metadata special characters.
  Returns nil if no such character is present."
  [line]
  (let [indices (filter
                  #(not (nil? %))
                  (map #(str/index-of line %) ["@" "#" "~" "-"]))
        idx (or (when (not-empty indices) (apply min indices))
                nil)]
    idx))

(defn- extract-item [str regex mapper]
  (let [[result & groups] (re-find regex str)
        l (count result)
        m (when (> l 0)
            (mapper groups))]                               ; 1st for complex 3th for simple
    [l m]))

(defn- item
  "Extract a string with special meaning from a
  string. The function assumes the string starts
  with one of the special CL characters.

  Depending on the special character a specific
  match, extract and format sequence is executed.
  Currently, all cases are mutually exclusive
  and only lead to a single type of special
  token."
  [str]
  {:pre  [(string? str)]
   :post [(s/valid? (s/nilable :recipe/item) (second %))]}
  (let [c (first str)]
    (cond
      (= c \#) (extract-item
                 str
                 re/cookware
                 #(hash-map :cookware (or (first %) (nth % 2 nil))))
      (= c \@) (extract-item
                 str
                 re/ingredient
                 #(hash-map :ingredient (or (first %) (nth % 3 nil)) ; 1st for complex 4th for simple
                            :amount (second %)
                            :unit (nth % 2 nil)))
      (= c \~) (extract-item
                 str
                 re/timer
                 #(hash-map :name (first %)
                            :amount (nth % 1 nil)
                            :unit (nth % 2 nil)))
      (= c \-) (extract-item
                 str
                 re/comment
                 #(hash-map :comment (first %)))
      )))

(defn parse-step
  "Parse a CL recipe step.
  A step is split into a sequence of tokens.
  Strings without special meaning are added as
  text tokens. Special tokens are added according
  to their specified formats.

  The function loops through the string, at each iteration
  the index of the next special character is retrieved.
  The text since the last special token is added
  as text-token, the token belonging to the special
  is added and the loop continues from the string
  after the end of the special token."
  [line]
  {:pre  [(string? line)]
   :post [(s/valid? :recipe/step %)]}
  (loop [l line
         parsed []]
    (let [idx (next-special-char l)]
      (cond
        (empty? l) parsed
        (nil? idx) (conj parsed {:text l})
        :else (let [[length i] (item (subs l idx))
                    text (str (subs l 0 idx) (when (nil? i) (nth l idx)))
                    parsed (cond-> parsed
                                   (not-empty text) (conj {:text text}) ; add text
                                   i (conj i))              ; add item
                    new-idx (+ idx (max length 1))]
                (recur (subs l new-idx) parsed)))           ; always continue 1 character further
      )))

(defn parse-line
  "Parse a single line in a recipe.
  Depending on the start of the line it is parsed as:
   - Metadata
   - Step
   - Empty

   It is possible to pass functions to execute
   computations on the result of parsing a line.
   By default, the result is returned.
   "
  ([line meta-cb step-cb default-cb]
   (cond
     (str/blank? line) (default-cb)
     (str/starts-with? line ">>") (apply meta-cb (extract-metadata line))
     :else (step-cb (parse-step line))))

  ([line]
   (parse-line line
               #(identity [%1 %2])
               #(identity %)
               #(identity %)))
  )

(defn- parse-recipe-line
  [recipe line]
  {:pre [(string? line)]}
  (let [add-meta #(assoc-in recipe [:metadata %1] %2)
        add-step #(assoc recipe :steps (conj (:steps recipe) %))
        ignore #(identity recipe)]
    (parse-line line add-meta add-step ignore)))

(defn parse-recipe-lines
  [lines]
  {:post [(s/valid? :recipe/recipe %)]}
  (reduce parse-recipe-line
          {:metadata {} :steps []}
          lines))

(defn parse-cooklang-file
  "Parse a cooklang file.
  Loops over all lines and adds the extracted
  steps and metadata to the recipe."
  [filename]
  (with-open [rdr (io/reader filename)]
    (parse-recipe-lines (line-seq rdr))))