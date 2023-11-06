(defproject cambium/cambium.logback.core "0.4.5"
  :description "Core Logback backend for Cambium"
  :url "https://github.com/cambium-clojure/cambium.logback.core"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.codehaus.janino/janino     "3.1.10"]  ; for conditional config processing
                 [ch.qos.logback/logback-classic "1.2.12"]
                 [ch.qos.logback/logback-core    "1.2.12"]]
  :java-source-paths ["java-src"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :global-vars {*warn-on-reflection* true
                *assert* true
                *unchecked-math* :warn-on-boxed}
  :profiles {:provided {:dependencies [[org.clojure/clojure  "1.6.0"]]}
             :dev {:dependencies [[cambium/cambium.core "1.1.1"]  ; pulls in [org.slf4j/slf4j-api "1.7.32"]
                                  [cambium/cambium.codec-simple "1.0.0"]]
                   :jvm-opts ["-Denable.dummy=true"]}
             :c06 {:dependencies [[org.clojure/clojure  "1.6.0"]]}
             :c07 {:dependencies [[org.clojure/clojure  "1.7.0"]]}
             :c08 {:dependencies [[org.clojure/clojure  "1.8.0"]]}
             :c09 {:dependencies [[org.clojure/clojure  "1.9.0"]]}
             :c10 {:dependencies [[org.clojure/clojure  "1.10.1"]]}
             :c11 {:dependencies [[org.clojure/clojure  "1.11.1"]]}
             :dln {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :aliases {"test-all" ["with-profile" "c06,dev:c07,dev:c08,dev:c09,dev:c10,dev:c11,dev" "test"]})
