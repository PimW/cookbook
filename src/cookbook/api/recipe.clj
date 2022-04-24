(ns cookbook.api.recipe
  (:use [hiccup.core]
        [cookbook.spec])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cookbook.cooklang.parser.parser :as p]
            [cookbook.api.views.recipe :as recipe-view]
            [cookbook.api.views.overview :as overview-view]))

(defonce recipes
         {"coffee-souffle"                 {:title  "Coffee Souffle"
                                            :recipe (p/parse-cooklang-file "resources/Coffee Souffle.cook")}
          "easy-pancakes"                  {:title  "Easy Pancakes"
                                            :recipe (p/parse-cooklang-file "resources/Easy Pancakes.cook")}
          "fried-rice"                     {:title  "Fried Rice"
                                            :recipe (p/parse-cooklang-file "resources/Fried Rice.cook")}
          "olivier-salad"                  {:title  "Olivier Salad"
                                            :recipe (p/parse-cooklang-file "resources/Olivier Salad.cook")}
          "creamy-instant-pot-pasta-salad" {:title  "Creamy Instant Pot Pasta"
                                            :recipe (p/parse-cooklang-file "resources/Creamy Instant Pot Pasta.cook")}
          })

(def recipe-urls (map #(vec [(first %) (:title (second %))]) recipes))

(defroutes main-routes
           (GET "/" [] (overview-view/overview-page recipe-urls))
           (GET "/:id" [id] (apply recipe-view/recipe-page
                                   (map (get recipes id)
                                        [:title :recipe])))
           (route/resources "/")
           (route/not-found "Page not found"))