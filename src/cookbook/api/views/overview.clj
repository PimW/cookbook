(ns cookbook.api.views.overview
  (:use [hiccup.core]))


(defn overview-page [recipes]
  (html
    [:head
     [:link {:rel "stylesheet" :href "https://unpkg.com/@picocss/pico@latest/css/pico.min.css"}]
     [:title "Recipe overview"]]
    [:body
     [:article#article {:style "margin: 0 auto; width: 60%"}
      [:h1 "Recipe overview"]
      [:ul (for [[id title] recipes]
             [:li [:a {:href id} title]])]
      ]]))