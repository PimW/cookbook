(ns cookbook.core-test
  (:require [clojure.test :refer :all]
            [cookbook.core :refer :all]))

(deftest ingredient-regex-test
  (testing "multiword ingredient regex"
    (is (=
          (extract-ingredients "@eggwhite{1}")
          [{:ingredient "eggwhite"
            :amount     "1"
            :unit       nil}])))

  (testing "multiword regex"
    (is (=
          (extract-ingredients "@eggwhite{}")
          [{:ingredient "eggwhite"
            :amount     nil
            :unit       nil}])))


  (testing "multiword regex"
    (is (=
          (extract-ingredients "@eggwhite{1%g}")
          [{:ingredient "eggwhite"
            :amount     "1"
            :unit       "g"}])))

  (testing "multiword regex"
    (is (=
          (extract-ingredients "@eggwhite")
          [{:ingredient "eggwhite"
            :amount     nil
            :unit       nil}])))

  (testing "simple ingredient regex"
    (is (=
          (extract-ingredients "@eggwhite @potato")
          [{:ingredient "eggwhite"
            :amount     nil
            :unit       nil}
           {:ingredient "potato"
            :amount     nil
            :unit       nil}]))
    )
  )

(deftest cookware-regex-test
  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan{}")
          [{:cookware "pan"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan{ }")
          [{:cookware "pan"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#big pan{}")
          [{:cookware "big pan"}]))))

(deftest cookware-regex-test
  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan{}")
          [{:cookware "pan"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan{} #pot{}")
          [{:cookware "pan"} {:cookware "pot"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan #pot")
          [{:cookware "pan"} {:cookware "pot"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#pan{ }")
          [{:cookware "pan"}])))

  (testing "multiword cookware regex"
    (is (=
          (extract-cookware "#big pan{}")
          [{:cookware "big pan"}]))))
