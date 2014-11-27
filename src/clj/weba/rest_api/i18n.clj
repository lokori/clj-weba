(ns weba.rest-api.i18n
  (:import (java.util Locale
                      ResourceBundle))
  (:require [compojure.core :as c]
            [schema.core :as schema]
            [weba.rest-api.http-util :refer [json-response-nocache]]
            [weba.util :refer [pisteavaimet->puu]]))

(defn validate-lang []
  (schema/pred (fn[k] (or (= k "fi")(= k "sv")))))

(defn fetch-texts [lang]
  (ResourceBundle/clearCache)
  (let [bundle (ResourceBundle/getBundle "i18n/labels" (Locale. lang))]
    (->> (for [key (.keySet bundle)]
           [(keyword key) (.getString bundle key)])
         (into {})
         pisteavaimet->puu)))

(c/defroutes routes
  (c/GET "/:lang" [lang :as req]
    (schema/validate (validate-lang) lang )
    (json-response-nocache (fetch-texts lang))))
