(ns cookbook.core
  "Cooklang parsing

  A cooklang file is formatted according to:

  The internal representation of a cooklang recipe is
  of the format:
  > Recipe
    > metadata {}
       > key: value
    > steps []
       > step []
          > text
          > comment
          > ingredient
          > cookware
          > timer"
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

(defn- print-if-not-nil [str]
  (if (not (nil? str))
    (println "- comment: " str)))

(defn ingredient-str [ingredient]
  (str (:amount ingredient)
       (if (not (nil? (:unit ingredient)))
         (str (:unit ingredient) " of ")
         " ")
       (:ingredient ingredient)))

(defn cookware-str [cookware]
  (:cookware cookware))

(defn time-str [time]
  (str (:amount time) " "
       (:unit time)
       (if (not (nil? (:name time)))
         (str " (" (:name time) ")"))
       ))

(defn extract-comments
  [line]
  (->> line
       (re-find comment-regex)
       (second)
       (print-if-not-nil)))

(defn extract [regex mapper line]
  (let [items (re-seq regex line)]
    (map #(vector (mapper %) (first %)) items))
  )

(defn extract-cookware [line]
  (extract
    cookware-regex
    #(hash-map
       :cookware (or (second %) (nth % 3 nil)))             ; 2nd for complex 4th for simple
    line))

(defn extract-ingredients [line]
  (extract
    ingredient-regex
    #(hash-map
       :ingredient (or (second %) (nth % 4 nil))            ; 2nd for complex 5th for simple
       :amount (nth % 2 nil)
       :unit (nth % 3 nil))
    line))

(defn extract-metadata [line]
  (extract
    metadata-regex
    #(hash-map
       :key (second %)
       :value (nth % 2 nil))
    line))

(defn next-special-char
  ""
  [line]
  (let [indices (filter
                  #(not (nil? %))
                  (map #(str/index-of line %) ["@" "#" "~" ">" "-"]))
        idx (or (when (not-empty indices) (apply min indices))
                nil)]
    idx))

(defn- item
  ; TODO: non-matching regex
  [str]
  (let [c (first str)]
    (cond
      (= c \#) (let [[result & g] (re-find cookware-regex str)
                     l (count result)
                     m {:cookware (or (first g) (nth g 2 nil))}]
                 [l m])
      (= c \@) (let [[result & g] (re-find ingredient-regex str)
                     l (count result)
                     m {:ingredient (or (first g) (nth g 3 nil)) ; 2nd for complex 5th for simple
                        :amount     (second g)
                        :unit       (nth g 2 nil)}]
                 [l m])
      (= c \~) (let [[result & g] (re-find time-regex str)
                     l (count result)
                     m {:name   (first g)
                        :amount (nth g 1 nil)
                        :unit   (nth g 2 nil)}]
                 [l m])
      ;(= c ">")
      (= c \-) (let [[result & g] (re-find comment-regex str)
                      l (count result)
                      m {:comment (first g)}]
                  [l m])
      )))


(defn parse-step [line]
  (println line)
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
                (recur (subs l (+ idx length)) parsed))
        )
      )))

; TODO: generic formatter?
(defn step-to-cooklang [parsed-step]
  (let [text-to-cl #(:text %)
        cookware-to-cl #(str "#" (:cookware %))
        comment-to-cl #(str "-- " (:comment %))
        timer-to-cl #(str "~" (:name %)
                          "{" (:amount %) (when (not (nil? (:unit %))) (str "%" (:unit %))) "}")
        ingredient-to-cl #(str "@" (:ingredient %)
                               "{" (:amount %) (when (not (nil? (:unit %))) (str "%" (:unit %))) "}")
        item-to-cl #(cond
                      (contains? % :cookware) (cookware-to-cl %)
                      (contains? % :text) (text-to-cl %)
                      (contains? % :name) (timer-to-cl %)
                      (contains? % :ingredient) (ingredient-to-cl %)
                      (contains? % :ingredient) (ingredient-to-cl %)
                      (contains? % :comment) (comment-to-cl %)
                      :else ""
                      )]
    (reduce str (map item-to-cl parsed-step))))

(defn parse-line
  "Parse a single line in a recipe.
  Depending on the start of the line it is parsed as:
   - Metadata
   - Step

  Empty lines are ignored"
  [line]
  (cond
    (str/blank? line) nil
    (str/starts-with? line ">>") nil
    :else (parse-step line)))

(defn parse-cooklang-file
  "docstring"
  [filename]
  (with-open [rdr (io/reader filename)]
    (reduce conj [] (filter
                      #(not (nil? %))
                      (map parse-line (line-seq rdr))))
    ))