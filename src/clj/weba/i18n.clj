(ns weba.i18n
  (:import (java.util Locale
                      ResourceBundle))
  (:require [clojure.string :as s]
            [ring.util.response :refer [redirect]]
            [compojure.core :as c]))

(def ^:dynamic *locale*)

(defn lang-and-uri
  "Separates URI into language code and the rest."
  [kysely]
  (rest (re-matches #"(?:/(fi|sv))?(/.*)" (:uri kysely))))

(defn accept-language->lang
  "Returns the first supported language code from Accept-Language HTTP header."
  [kysely]
  (when-let [accept-language ((:headers kysely) "accept-language")]
    (let [kielet (s/split accept-language #" ")
          kielet-ilman-qta (map #(s/replace % #";.*" "") kielet)]
      (some #{"fi" "sv"} kielet-ilman-qta))))

(defn wrap-locale [ring-handler & {:keys [no-redirect, base-url]}]
  (fn [request]
    (let [[uri-lang uri] (lang-and-uri request) 
          browser-lang (accept-language->lang request)
          lang (or uri-lang browser-lang)
          no-redirect? (some-> no-redirect (re-matches (:uri request)))]
      (if (or lang no-redirect?)
        (with-bindings (if lang
                         {#'*locale* (Locale. lang)}
                         {})
          (ring-handler (assoc request :uri uri)))
        (redirect (str base-url "/" "fi" uri))))))
