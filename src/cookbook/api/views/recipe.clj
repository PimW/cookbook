(ns cookbook.api.views.recipe
  (:use [hiccup.core])
  (:require [cookbook.cooklang.recipe :as recipe]))


(defn metadata [m]
  [:div
   [:h5 "Metadata"]
   [:ul (for [[k v] m]
          [:li (str k ": " v)])]])

(defn ingredient-list [ingredients]
  [:div
   [:h2 "Ingredients"]
   [:ul (for [i ingredients]
          [:li (str (:amount i)
                    (when (not (nil? (:unit i))) (str " " (:unit i)))
                    " "
                    (:ingredient i))])]])

(defn ingredient [i]
  (str (:amount i)
       (when (not (nil? (:unit i))) (:unit i))
       " ")
  [:kbd {:style "padding: .05rem .3rem"} (:ingredient i)])

(defn cookware [c]
  [:kbd {:style "padding: .05rem .3rem;background:orange"} (:cookware c)])

(defn recipe-comment [c]
  [:cite {:style "opacity: 50%"} (str "- " (:comment c))])

(defn timer [t]
  (str (:amount t) " "
       (:unit t)
       (if (not (nil? (:name t)))
         (str " (" (:name t) ")"))
       ))

(defn recipe-step [step]
  (for [part step]
    (cond
      (contains? part :text) (:text part)
      (contains? part :ingredient) (ingredient part)
      (contains? part :cookware) (cookware part)
      (contains? part :comment) (recipe-comment part)
      (contains? part :name) (timer part)
      :else [:kbd {:style "background:red"} "MISSING"]
      )))

(defn recipe-steps [steps]
  [:div
   [:h2 "Steps"]
   [:ul (for [s steps]
          [:li (recipe-step s)])]])

(defn recipe-page [title recipe]
  (html
    [:head
     [:link {:rel "stylesheet" :href "https://unpkg.com/@picocss/pico@latest/css/pico.min.css"}]
     [:title title]]
    [:body
     [:article#article {:style "margin: 0 auto; width: 60%"}
      [:div {:style "display:flex;justify-content:right"}
       [:a {:href "/"} "Back"]]
      [:h1 title]
      (metadata (:metadata recipe))
      (ingredient-list (recipe/ingredients recipe))
      (recipe-steps (:steps recipe))
      ]]))
