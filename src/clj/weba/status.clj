(ns weba.status
  (:require [clojure.java.io :as io]))

;; Postwalk käsittelee yksittäisen avain-arvo-parin vektorina, ei MapEntryna.
(defn hide-passwords [status]
  (clojure.walk/postwalk #(if (and (vector? %)
                                   (= :password (first %)))
                            [:password "*****"]
                            %)
                         status))

(def build-id (delay (if-let [resource (io/resource "build-id.txt")]
                       (.trim (slurp resource))
                       "dev")))

(defn status []
  {:build-id @build-id
   :install-history (try
                      (slurp "install-history.txt")
                      (catch java.io.FileNotFoundException _
                        nil))})
