(ns moodseer-clj.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [moodseer-clj.core :refer :all]))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

(deftest another-test
  (testing "Random API test"
    (is 
     (= "Do something with req"
        (-> (request :get "/" )
            app
            :body)))))
