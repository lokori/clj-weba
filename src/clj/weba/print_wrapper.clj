(ns weba.print-wrapper
  "Logitus HTTP pyynnöille Ringiin. Vastaavia yleiskäyttöisiäkin on, mutta niissä on toivomisen varaa ainakin tällä hetkellä."
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def ^:private requestid (atom 1))
(def ^:dynamic *requestid*)

(defn debug-request
  "Ring wrapper joka logittaa kaiken informaation requestista"
  [ring-handler]
  (fn [request]
    (log/info request)
    (ring-handler request)))

(defn http-method->str [keyword-or-str]
  (str/upper-case (name keyword-or-str)))

(defn log-request-wrapper [ring-handler & custom-paths-vseq]
  "Logitus requestille. Perustiedot + kestoaika ja uniikki id per request"
  (fn [req]
    (binding [*requestid* (swap! requestid inc)]
      (let [start (System/currentTimeMillis)
            host (or (get-in req [:headers "x-forwarded-host"])
                     (get-in req [:headers "host"]))]
        (log/info (str "Request " *requestid* " start. "
                       " host: " host
                       " ,remote-addr: " (:remote-addr req)
                       " ,method: " (http-method->str (:request-method req))
                       " ,uri: " (:uri req)
                       " ,query-string: " (:query-string req)
                       " ,user-agent: " (get (:headers req) "user-agent")
                       " ,referer: " (get (:headers req) "referer")
                       " ,oid: " (get (:headers req) "oid")))
        (let [response (ring-handler req)
              finish (System/currentTimeMillis)
              total  (- finish start)]
          (if (or (= response nil)
                  (= (:status response) 404))
            (log/warn (str "Response nil or status 404, uri: " (:uri req) ", query-string: " (:query-string req))))
          (log/info (str "Request " *requestid* " end. Status: " (:status response) " Duration: " total " ms. uri: " (:uri req)))
          response)))))
