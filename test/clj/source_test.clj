(ns source-test
  "Source code checking."
  (:import java.io.PushbackReader)
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :refer [file reader]]
            [clojure.walk :as cw]
            [clojure.string :refer [trim]]))

(defn files [dir path-re skip]
  (let [skip (set (map file skip))]
    (for [path (file-seq (file dir))
          :when (not (or (.isDirectory path)
                         (skip path)))
          :when (re-matches path-re (str path))]
      path)))

(defn matching-files [dir path-re f & {:keys [skip]
                                                    :or {skip #{}}}]
  (apply concat
    (for [path (files dir path-re skip)]
       (with-open [r (reader path)]
         (f r)))))

(defn matching-lines [dir path-re mallit & {:keys [skip]
                                                   :or {skip #{}}}]
  (apply concat
         (for [path (files dir path-re skip)]
           (with-open [r (reader path)]
             (doall
               (for [[nro rivi] (map vector
                                     (iterate inc 1)
                                     (line-seq r))
                     :when (some #(re-find % rivi) mallit)]
                 (str path ":" nro ": " (trim rivi))))))))

(defn matching-forms [dir ehto & {:keys [skip path-re]
                                          :or {skip #{}
                                               path-re #".*\.clj"}}]
    (apply concat
           (for [path (files dir path-re skip)]
             (with-open [r (PushbackReader. (reader path))]
               (doall
                 (for [form (repeatedly #(read r false ::eof))
                       :while (not= form ::eof)
                       :when (ehto form)]
                   (str path ": " form)))))))


(defn pre-post [form]
  (when (= 'defn (nth form 0))
    (some #(and (map? %)
                (or (contains? % :pre)
                    (contains? % :post))
                %)
          form)))

(defn pre-post-in-wrong-place? [form]
  (when-let [pp (pre-post form)]
    (not (or (and (symbol? (nth form 1))
                  (vector? (nth form 2))
                  (= pp (nth form 3)))
             (and (symbol? (nth form 1))
                  (string? (nth form 2))
                  (vector? (nth form 3))
                  (= pp (nth form 4)))))))

(defn pre-post-not-vector? [form]
  (when-let [pp (pre-post form)]
    (not (every? vector? (vals pp)))))

(deftest js-debug-test
  "console.log doesn't work without developer tools in all browsers. Therefore it's a bad thing to have in Javascript source files."
  (is (empty? (matching-lines "resources/public/js"
                               #".*\.js"
                               [#"console\.log"
                                #"debugger"
                                (re-pattern (str \u00a0)) ; non-breaking space
                                ]
                               :skip ["resources/public/js/vendor/angular.js"
                                       "resources/public/js/vendor/stacktrace.js"]))))
 
(deftest properties-encoding-test
  "searches for characters which are not printable characters in the properties files." 
  (is (empty? (matching-lines "resources/i18n"
                               #".*\.properties"
                               [#"[^\p{Print}\p{Space}]+"]))))

(defn properties-duplicat-keys? [r]
  (let [dup (doto (util.DuplicateAwareProperties.)
                  (.load r)
                  )
        duplicates (.getDuplicates dup)]
    duplicates))

(deftest properties-duplicate-keys-test
  "Searches properties files for duplicate keys"
  (is (empty? (matching-files "resources/i18n" #".*\.properties"
                properties-duplicat-keys?))))

(deftest pre-post-in-correct-place
  "Pre/post condition should be in the correct position. If not, compiler will not usually complain but code doesn't work."
  (is (empty? (matching-forms "src/clj" pre-post-in-wrong-place?))))

(deftest pre-post-vector-test
  "Pre/post condition should be a vector. Compiler doesn't warn if it's something else, but it will not work as intended."
  (is (empty? (matching-forms "src/clj" pre-post-not-vector?))))

(defn load-props [filename]
  (with-open [fs (java.io.FileInputStream. filename)]
    (let [ prop (java.util.Properties.)]
      (.load prop fs)
      (into {} prop))))

(deftest languages-have-matching-properties
  (testing "Checks that each finnish localization key is matched by a corresponding swedish localization key"
    (let [suomi-avaimet  (set (keys (load-props "resources/i18n/labels.properties")))
          ruotsi-avaimet (set (keys (load-props "resources/i18n/labels_sv.properties")))]
      (is (empty? (clojure.set/difference suomi-avaimet ruotsi-avaimet))))))
