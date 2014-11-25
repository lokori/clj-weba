(ns rest.statuspage-test
   (:require
    [weba.settings :as app-settings]
    [weba.server :as server]
    [clojure.test :refer :all]
    [peridot.core :as peridot]))

(defn peridot-session! []
  (let [settings
        (-> app-settings/defaultsettings
          (assoc :development-mode true))
        crout (server/app settings)
        _ (deliver app-settings/settings settings)]
    (peridot/session crout)))

(deftest statuspage-test []
  (testing "The heartbeat URL (status page) is responding"
    (let [peridot-session (peridot-session!)
          url "http://localhost:8081/fi/status"
          response (:response (peridot/request peridot-session url))]
      (is (= (:status response) 200)))))