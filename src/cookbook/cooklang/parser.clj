(ns cookbook.cooklang.parser
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
            [clojure.string :as str]))

(def comment-regex (re-pattern #"^\s*--\s*(.*)$"))
(def metadata-regex (re-pattern #"^>>\s*(?<key>.+?):\s*(?<value>.+)"))
(def time-regex (re-pattern #"^~([^#~@\{]+)?\{(?<time>[^\}]*?)(?:%(?<unit>[^}]+)?)?\}"))

(def ingredient-regex
  "Matches ingredients of type:
  - @eggwhite{}
  - @eggwhite{1}
  - @eggwhite{1%g}
  - @egg white{1}
  - @egg"
  (re-pattern #"^@([^#~@\{]+)(?:\{(?<amount>[^\}]+?)?(?:%(?<unit>[^}]+?)?)?\})|@([^\s#~@\{]+)"))

(def cookware-regex
  "Matches cookware of the type:
  - #pan{}
  - #pan{ }
  - #big pan{}
  - #pan"
  (re-pattern #"^#([^#~@\{]+)\{([^\}]*)\}|#([^\s#~@\{]+)"))

(defn extract-metadata
  "Extract a line of metadata of the form:
  >> <key>: <value>"
  [line]
  (let [res (re-find metadata-regex line)]
    [(second res) (nth res 2 nil)]))

(defn next-special-char
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

(defn- item
  "Extract a string with special meaning from a
  string. The function assumes the string starts
  with one of the special CL characters.

  Depending on the special character a specific
  match, extract and format sequence is executed.
  Currently, all cases are mutually exclusive
  and only lead to a single type of special
  token."
  ; TODO:
  ;  - failed regex
  ;  - missing character
  [str]
  (let [c (first str)]
    (cond
      (= c \#) (let [[result & g] (re-find cookware-regex str)
                     l (count result)
                     m {:cookware (or (first g) (nth g 2 nil))}] ; 1st for complex 3th for simple
                 [l m])
      (= c \@) (let [[result & g] (re-find ingredient-regex str)
                     l (count result)
                     m {:ingredient (or (first g) (nth g 3 nil)) ; 1st for complex 4th for simple
                        :amount     (second g)
                        :unit       (nth g 2 nil)}]
                 [l m])
      (= c \~) (let [[result & g] (re-find time-regex str)
                     l (count result)
                     m {:name   (first g)
                        :amount (nth g 1 nil)
                        :unit   (nth g 2 nil)}]
                 [l m])
      (= c \-) (let [[result & g] (re-find comment-regex str)
                     l (count result)
                     m {:comment (first g)}]
                 [l m])
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
  (loop [l line
         parsed []]
    (let [idx (next-special-char l)]
      (cond
        (empty? l) parsed
        (nil? idx) (conj parsed {:text l})
        :else (let [text (subs l 0 idx)
                    parsed (if (not-empty text)
                             (conj parsed {:text text})
                             parsed)
                    [length i] (item (subs l idx))
                    parsed (conj parsed i)]
                (recur (subs l (+ idx length)) parsed)))
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

(defn parse-cooklang-file
  "Parse a cooklang file.
  Loops over all lines and adds the extracted
  steps and metadata to the recipe."
  [filename]
  (with-open [rdr (io/reader filename)]
    (loop [recipe {:metadata {}
                   :steps    []}
           lines (line-seq rdr)]
      (if (empty? lines)
        recipe
        (let [[line & more] lines
              new-recipe (parse-line
                           line
                           #(assoc recipe :metadata (assoc (:metadata recipe) %1 %2))
                           #(assoc recipe :steps (conj (:steps recipe) %))
                           #(identity recipe))]
          (recur new-recipe more)))
      )))