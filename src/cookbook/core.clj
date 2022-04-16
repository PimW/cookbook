(ns cookbook.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))


(def comment-regex (re-pattern #"--\s*(.*)$"))
(def metadata-regex (re-pattern #"^>>\s*(?<key>.+?):\s*(?<value>.+)"))
(def time-regex (re-pattern #"~([^#~@\{]+)?\{(?<time>[^\}]*)(?:%(?<unit>[^}]+?)?)\}"))

(def ingredient-regex
  "Matches ingredients of type:
  - @eggwhite{}
  - @eggwhite{1}
  - @eggwhite{1%g}
  - @egg white{1}
  - @egg"
  (re-pattern #"@([^#~@\{]+)(?:\{(?<amount>[^\}]+?)?(?:%(?<unit>[^}]+?)?)?\})|@([^\s#~@\{]+)"))

(def cookware-regex
  "Matches cookware of the type:
  - #pan{}
  - #pan{ }
  - #big pan{}
  - #pan"
  (re-pattern #"#([^#~@\{]+)\{([^\}]*)\}|#([^\s#~@\{]+)"))

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

(defn extract-time [line]
  (extract
    time-regex
    #(hash-map
       :name (second %)
       :amount (nth % 2 nil)
       :unit (nth % 3 nil))
    line))

(defn extract-cookware [line]
  (extract
    cookware-regex
    #(hash-map
       :cookware (or (second %) (nth % 3 nil))) ; 2nd for complex 4th for simple
    line))

(defn extract-ingredients [line]
  (extract
    ingredient-regex
    #(hash-map
       :ingredient (or (second %) (nth % 4 nil)) ; 2nd for complex 5th for simple
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

(defn next-special-char [line]
  (let [idx (apply min
                   (filter
                     #(not (nil? %))
                     (map #(str/index-of line %) ["@" "#" "~" ">" "-"])))
        character (nth line idx)]
    [idx character]))



(defn parse-line [line]
  (loop [l line
         parsed []]
    (let [[idx character] (next-special-char line)
          text (subs line 0 idx)
          parsed (conj parsed {:text text})] ; add text before token
      ; add special value
      (recur (subs line idx) parsed)) ; TODO update idx
    ))
;  ; tokenized-line = []
;  ; while find-next-token
;  ;  put text in tokenized line
;  ;  put token in tokenized line
;  (loop []
;    (re-find ))
;  )

(defn parse-old [line]
  (extract-metadata line)
  (extract-comments line)
  (doseq [ingredient (extract-ingredients line)]
    (println "-i " (ingredient-str ingredient)))
  (doseq [cookware (extract-cookware line)]
    (println "-c " (cookware-str cookware)))
  (doseq [time (extract-time line)]
    (println "-t " (time-str time))))

(defn parse-cooklang-file
  "docstring"
  [filename]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (parse-line line))))