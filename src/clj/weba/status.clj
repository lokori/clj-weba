(ns weba.status
  "Provides access to status and configuration of the software."
  (:require [clojure.java.io :as io]))

;; postwalk handles a single key-value pair as a vector, not as a MapEntry
(defn hide-passwords
  "Passwords definitely should not be displayed in plain text. There might be other settings to mask too."
  [status]
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
