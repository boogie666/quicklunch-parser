(defproject quick-lunch-parser-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "clojure-getting-started-standalone.jar"
  :profiles {:production {:env {:port "80"
                                :join? true}}
             :dev {:env {:port "8080"
                         :join? false}}}
  :plugins [[lein-environ "1.1.0"]]
  :main quick-lunch-parser-clj.core
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [enlive "1.1.6"]
                 [http-kit "2.1.18"]
                 [compojure "1.6.0"]
                 [ring/ring-devel "1.6.2"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [environ "1.1.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/core.cache "0.6.5"]])
