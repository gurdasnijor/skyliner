(defproject hello-clojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :repositories [["clojars" {:url "https://repo.clojars.org"}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.0-beta1" :exclusions [ring/ring-core]]
                 [org.eclipse.jetty/jetty-server "9.3.14.v20161028"]
                 [ring "1.6.0-beta6"]
                 [ring/ring-defaults "0.3.0-beta1"]
                 [spootnik/unilog "0.7.15"]]
  :plugins [[lein-ring "0.9.7"]]
  :main ^:skip-aot hello-clojure.main
  :target-path "target/%s/" ; don't get AOT in your REPL
  :aliases {"package" ["do"
                       ["test"]
                       ["uberjar"]]}
  :profiles
  {:dev         {:dependencies [[ring/ring-mock "0.3.0"]]
                 :repl-options {:init-ns user}
                 :source-paths ["dev"]}
   :uberjar     {:aot      :all
                 :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
