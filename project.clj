(defproject weba "0.1.0-SNAPSHOT"
  :description "Web app archetype"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.1.8"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.2.0"]
                 [stencil "0.3.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [clj-time "0.8.0"]
                 [com.cemerick/valip "0.3.2"]
                 [prismatic/schema "0.2.0"]
                 [stencil "0.3.2"]
                 [peridot "0.3.0"]

                 [org.clojure/core.typed "0.2.61"]
                 [org.clojars.lokori/lolog "0.1.0"]]
  :plugins [[test2junit "1.0.1"]
            [lein-typed "0.3.5"]
	    [jonase/eastwood "0.2.3"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/test.check "0.5.8"]
				  [clj-gatling "0.4.0"]]}
             :uberjar {:main weba.server
                       :aot :all}}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]  
  :test-paths ["test/clj"]
  :test-selectors {:all (constantly true)
                   :default  (complement (some-fn :integration :performance)) 
                   :performance :performance
                   :integration :integration}
  :main weba.server
  :repl-options {:init-ns user}
  :jar-name "weba.jar"
  :uberjar-name "weba-standalone.jar")

