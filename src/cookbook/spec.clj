(ns cookbook.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :property/comment string?)
(s/def :property/ingredient string?)
(s/def :property/amount (s/nilable string?))
(s/def :property/unit (s/nilable string?))
(s/def :property/cookware string?)
(s/def :property/name (s/nilable string?))
(s/def :property/text string?)

(s/def :recipe/ingredient
  (s/keys :req-un [:property/ingredient
                   :property/amount
                   :property/unit]))

(s/def :recipe/cookware
  (s/keys :req-un [:property/cookware]))

(s/def :recipe/timer
  (s/keys :req-un [:property/name
                   :property/amount
                   :property/unit]))

(s/def :recipe/text
  (s/keys :req-un [:property/text]))

(s/def :recipe/comment
  (s/keys :req-un [:property/comment]))

(s/def :recipe/item
  (s/or :text :recipe/text
        :ingredient :recipe/ingredient
        :cookware :recipe/cookware
        :timer :recipe/timer
        :comment :recipe/comment))

(s/def :recipe/step (s/coll-of :recipe/item :kind vector))
(s/def :recipe/steps (s/coll-of :recipe/step :kind vector))

(s/def :recipe/metadata (s/map-of string? string?))

(s/def :recipe/recipe
  (s/keys :req-un [:recipe/metadata
                   :recipe/steps]))