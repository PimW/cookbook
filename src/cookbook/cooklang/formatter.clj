(ns cookbook.cooklang.formatter
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(defn step-formatter
  [format-text
   format-cookware
   format-comment
   format-timer
   format-ingredient]
  (let [format-item #(cond
                       (contains? % :cookware) (format-cookware %)
                       (contains? % :text) (format-text %)
                       (contains? % :name) (format-timer %)
                       (contains? % :ingredient) (format-ingredient %)
                       (contains? % :comment) (format-comment %)
                       :else ""
                       )]
    (fn [step] (reduce str (map format-item step)))))

(defn recipe-formatter [step-formatter metadata-formatter]
  (fn [recipe]
    {:pre [(s/valid? :recipe/recipe recipe)]
     :post [(string? %)]}
    (str/join
      "\n\n"
      (filter #(not (or (nil? %) (str/blank? %)))
              (into
                (vec (map metadata-formatter (:metadata recipe)))
                (map step-formatter (:steps recipe)))))))

; COOKLANG
(defn cooklang-metadata-formatter [metadata]
  (str ">> " (first metadata) ": " (second metadata)))

(def cooklang-step-formatter
  (step-formatter #(:text %)
                  #(str "#" (:cookware %))
                  #(str "-- " (:comment %))
                  #(str "~" (:name %)
                        "{" (:amount %) (when (not (nil? (:unit %))) (str "%" (:unit %))) "}")
                  #(str "@" (:ingredient %)
                        "{" (:amount %) (when (not (nil? (:unit %))) (str "%" (:unit %))) "}")
                  ))

(def recipe-to-cooklang
  "Formats a recipe as a Cooklang string"
  (recipe-formatter
    cooklang-step-formatter
    cooklang-metadata-formatter))

; String
(defn string-metadata-formatter [metadata]
  (str (str/capitalize (first metadata)) ": " (second metadata)))

(def string-step-formatter
  (step-formatter #(:text %)
                  #(:cookware %)
                  (fn [_] nil)
                  #(str (:amount %) " "
                        (:unit %)
                        (if (not (nil? (:name %)))
                          (str " (" (:name %) ")"))
                        )
                  #(str (:amount %)
                        (if (not (nil? (:unit %)))
                          (str (:unit %) " of ")
                          " ")
                        (:ingredient %))
                  ))

(def recipe-to-string
  "Formats a recipe as normal recipe"
  (recipe-formatter
    string-step-formatter
    string-metadata-formatter))