(ns weba.server
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [compojure.core :as c]
            [compojure.route :as r]
            [org.httpkit.server :as hs]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as resp]
            [cheshire.generate :as json-gen]
            schema.core
            [lolog.core :refer [wrap-log-request]]
            [weba.settings :refer [settings read-configuration defaultsettings configure-logging]]
            [weba.i18n :refer [wrap-locale]]
            [weba.status :refer [status hide-passwords build-id]]
            [stencil.core :as s]
            weba.rest-api.i18n
            weba.rest-api.js-log))

(schema.core/set-fn-validation! true)

(defn ^:private routes [settings]
  (c/routes
    (c/context "/api/i18n" [] weba.rest-api.i18n/routes)
    (c/context "/api/jslog" []  weba.rest-api.js-log/routes )
    (if (:development-mode settings)
      (c/GET "/status" [] (s/render-file "status" (assoc (status)
                                                         :settings (with-out-str
                                                                     (-> settings
                                                                       hide-passwords
                                                                       pprint)))))
     (c/GET "/status" [] (s/render-string "OK" {})))
     (c/GET "/" [] (s/render-file "public/app/index.html" {:base-url (-> settings :server :base-url)}))
    (r/not-found "Not found")))

(defn shutdown [server]
  (log/info "Shutting down server")
  ((:shutdown server))
  (log/info "Server shutdown ok"))

(defn app 
  "Application without HTTP bindings. Just the Ring stack"
  [settings]
  (json-gen/add-encoder org.joda.time.LocalDate
    (fn [c json-generator]
      (.writeString json-generator (.toString c "yyyy-MM-dd"))))
  (->
    (routes settings)
    wrap-keyword-params
    wrap-json-params
    (wrap-resource "public/app")
    (wrap-locale
      :no-redirect #"/api/.*"
      :base-url (get-in settings[:server :base-url]))
    wrap-params
    wrap-content-type
    wrap-log-request))

(defn start! [defaultsettings]
  (try
    (log/info "Starting server, version" @build-id)
    (let [configuration (read-configuration defaultsettings)
          _ (deliver settings configuration )
          _ (configure-logging configuration)
          port (get-in configuration [:server :port])
          shutdown (hs/run-server (app configuration)
                     {:port port})
          _ (log/info "Server started at port" port)]
      {:shutdown shutdown})
    (catch Throwable t
      (let [error-msg "Server start failed"]
        (log/error t error-msg)
        (binding [*out* *err*]
          (println error-msg))
        (.printStackTrace t *err*)
        (System/exit 1)))))

(defn -main []
  (start! defaultsettings))
