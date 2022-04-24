(ns cookbook.core
  (:use [cookbook.cooklang.recipe])
  (:use ring.adapter.jetty)
  (:require [cookbook.api.recipe :as recipe-api]))

(run-jetty recipe-api/main-routes
           {:port 3000 :join? false})