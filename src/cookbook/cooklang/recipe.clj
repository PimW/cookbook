(ns cookbook.cooklang.recipe)

(defn step-ingredients [step]
  (filter #(contains? % :ingredient) step))

(defn ingredients [recipe]
  (reduce into
          []
          (map step-ingredients (:steps recipe))))

(defn combined-ingredients [recipe]
  (let [grouped (group-by #(:ingredient %) (ingredients recipe))]
    (map (sort-by :unit grouped)
         )))