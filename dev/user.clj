(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :as nsr]
            clojure.core.cache
            schema.core
            stencil.loader))

(schema.core/set-fn-validation! true)

(defonce ^:private server (atom nil))

;; Template caching turned off during development
(stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))

(defn ^:private start! []
  {:pre [(not @server)]
   :post [@server]}
  (require 'weba.server)
  (reset! server ((ns-resolve 'weba.server 'start!)
                     (assoc @(ns-resolve 'weba.settings 'defaultsettings)
                            :development-mode true))))

(defn ^:private shutdown! []
  {:pre [@server]
   :post [(not @server)]}
  ((ns-resolve 'weba.server 'shutdown) @server)
  (reset! server nil))

(defn restart! []
  (when @server
    (shutdown!))
  (nsr/refresh :after 'user/start!))
