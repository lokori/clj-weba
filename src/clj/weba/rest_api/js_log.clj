(ns weba.rest-api.js-log
  (:require [compojure.core :as c]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;   "max length of message strings from the client side"
(def
  maxlength 1000)

(defn sanitize
  "replaces linefeeds with blanks and limits the length"
  [s]
  {:pre [(clojure.core/string? s)]}
  (let [ln (min (.length s) maxlength)]
    (-> s
      (str/replace "\n" "!")
      (str/replace "\r" "!")
      (.substring 0 ln))))

(c/defroutes routes
  (c/POST "/error" [virheenUrl userAgent message stackTrace cause]
    (log/info (str "\n"
                   "--- Javascript error ---" "\n"
                   "Error url: " (sanitize virheenUrl) "\n"
                   "User agent string: " (sanitize userAgent) "\n"
                   "Message: " (sanitize message) "\n"
                   "Stacktrace: " (sanitize stackTrace) "\n"
                   "Cause: " (sanitize cause) "\n"
                   "------------------------")))
    {:status 200})
