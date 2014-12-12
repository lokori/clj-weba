(ns rest.perf-test
  (:require
    [clojure.test :refer :all]))

(use 'clj-gatling.core)

(deftest ^:performance test-performance []
  (run-simulation
    [{:name "Localhost test cenario"
     :requests [{:name "Root request" :http "http://localhost:8081/fi/"}]}] 100))