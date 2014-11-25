(ns weba.settings
  "Configuration handling. Defines default settings, which can be overridden in an external properties file.
  Provides type checking and basic coercien for property values."
  (:require [clojure.java.io :refer [file]]
            clojure.set
            [clojure.tools.logging :as log]

            [weba.util :refer [pisteavaimet->puu
                               deep-merge
                               deep-update-vals
                               paths]]
            [schema.core :as s]
            [schema.coerce :as sc])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [org.slf4j LoggerFactory]))

(def Settings {:server {:port s/Int
                        :base-url s/Str}
               :development-mode Boolean
               :logback {:properties-file s/Str}
               :response-cache-max-age s/Int
               :db {:host s/Str
                    :port s/Int
                    :name s/Str
                    :user s/Str
                    :password s/Str
                    :maximum-pool-size s/Int
                    :minimum-pool-size s/Int}})

(defn string->boolean [x]
  (case x
    "true" true
    "false" false
    x))

(defn string-coercion-matcher [schema]
  (or (sc/string-coercion-matcher schema)
      ({Boolean string->boolean} schema)))

(defn coerce-settings [settings]
  ((sc/coercer Settings string-coercion-matcher) settings))

(def defaultsettings
  {:server {:port 8081
            :base-url ""}
   :development-mode false ; As a default we are not in the development mode. This is intentional.
   :logback {:properties-file "resources/logback.xml"}
   :response-cache-max-age 0
   :db {:host "127.0.0.1"
        :port 2345
        :name "master_fu"
        :user "app_user"
        :password "psst"
        :maximum-pool-size 15
        :minimum-pool-size 3}})

(def settings (promise))

(defn configure-logging
  "Configurates Logback based on an external file."
  [settings]
  (let [filepath (-> settings :logback :properties-file)
        config-file (file filepath)
        config-file-path (.getAbsolutePath config-file)
        configurator (JoranConfigurator.)
        context (LoggerFactory/getILoggerFactory)]
    (log/info "logback configuration reset: " config-file-path)
    (.setContext configurator context)
    (.reset context)
    (.doConfigure configurator config-file-path)))

(defn read-settings-from-file
  [path]
  (try
    (with-open [reader (clojure.java.io/reader path)]
      (doto (java.util.Properties.)
        (.load reader)))
    (catch java.io.FileNotFoundException _
      (log/info "Configuration file not found. Using default settings.")
      {})))

(defn get-settings
  [property-map]
  (->> property-map
     (into {})
     pisteavaimet->puu))

(defn read-configuration
  ([defaults] (read-configuration defaults "weba.properties"))
  ([defaults path]
    (->>
      (read-settings-from-file path)
      (get-settings)
      (deep-merge defaults)
      (coerce-settings))))
