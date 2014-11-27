(ns weba.util
  "General util functions."
  (:require [clojure.core.typed :as t]
            [cheshire.core :as cheshire]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clojure.string :as string]
            [org.httpkit.client :as http]
            [clojure.set :refer [union]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as log]))

(t/tc-ignore

;; http://clojuredocs.org/clojure_contrib/clojure.contrib.map-utils/deep-merge-with
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn last-arg [& args]
  (last args))

(def deep-merge (partial deep-merge-with last-arg))

(defn pisteavaimet->puu [m]
  (reduce #(let [[k v] %2
                 polku (map keyword (.split (name k) "\\."))]
             (assoc-in %1 polku v))
          {}
          m))

(defn ^:private nil-or-blank?
  "Returns true if input is nil or a blank string, false otherwise"
  [v]
  (if (string? v)
    (clojure.string/blank? v)
    (nil? v)))

(defn get-in-list
  "Like get-in, but also inspects each item of a list"
  ([m ks]
    (get-in-list m ks nil))
  ([m ks not-found]
    (loop [sentinel (Object.)
           m m
           ks (seq ks)]
      (if ks
        (let [m (get m (first ks) sentinel)
              ks (next ks)]
          (cond
            (identical? sentinel m) not-found
            (map? m) (recur sentinel m ks)
            (coll? m) (flatten (for [val m] (get-in-list val ks not-found)))
            :else (recur sentinel m ks)))
        m))))

(defn update-vals
  [f c]
  (reduce #(assoc %1 %2 (f (%1 %2)))
          c (vec (keys c))))

(defn deep-update-vals
  [f c]
  (reduce #(let [val (%1 %2)]
             (assoc %1 %2
                    (if (map? val)
                      (deep-update-vals f val)
                      (f val))))
          c (vec (keys c))))

(defn max-date
  ([a] a)
  ([a b]
    (if (< 0 (compare a b))
      a
      b))
  ([a b & more]
    (reduce max-date (max-date a b) more)))

(defn paths
  "Returns a set containing all paths pointing inside the map. Ie. a recursive description of the map's structure."
  ([m]
    (set (paths m [])))
  ([m ks]
    (apply concat
           (for [[k v] m
                 :let [path (conj ks k)]]
             (if (map? v)
               (conj (paths v path) path)
               [path])))))

(defn parse-ymd
  [ymd]
  (time-format/parse-local-date (time-format/formatters :year-month-day) ymd))

(defn diff-maps
  "Returns differences between the maps in the form {key [new-value old-value]} or nil if the maps are identical."
  [new-map old-map]
  (into {} (for [k (union (set (keys new-map))
                          (set (keys old-map)))
                :let [new-v (get new-map k)
                      old-v (get old-map k)]]
             [k (when (not= new-v old-v)
                  [new-v old-v])])))

(defn get-json-from-url
  [url]
  (-> @(http/get url)
    :body
    cheshire/parse-string
    keywordize-keys))

(defn newest-lastmodified
  "Returns the last modified time stamp from the given values.
   Polut are get-in style key-paths which define the route to the location of the last modified time stamp."
  [values & paths]
  (let [lastmodifieds (flatten
                        (for [value values
                              path paths]
                          (get-in-list value path)))]
    (reduce max-date (time/date-time 1970 1 1 0 0 1) lastmodifieds))))

(t/ann ^:no-check clojure.string/lower-case [String -> String])


(t/tc-ignore

(defn some-value [pred coll]
  (first (filter pred coll)))

(defn map-by [f coll]
  (into {} (for [item coll]
             [(f item) item])))

(defn retrying* [expected-throwable attempts f]
  {:pre [(isa? expected-throwable Throwable)
         (pos? attempts)]}
  (if (= attempts 1)
    (f)
    (try
      (f)
      (catch Throwable t
        (if (instance? expected-throwable t)
          (let [attempts-left (dec attempts)]
            (log/warn t "Operation failed, retrying.."
                      (if (= attempts-left 1)
                        "last time"
                        (str attempts-left " times")))
            (retrying* expected-throwable attempts-left f))
          (throw t))))))

(defmacro retrying [expected-throwable attempts & body]
  `(retrying* ~expected-throwable ~attempts (fn [] ~@body))))
