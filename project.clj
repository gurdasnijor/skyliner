(defproject my-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [metosin/compojure-api "1.1.8"]
                 [com.datomic/datomic-free "0.9.5544"]]
  :ring {
    :handler my-api.handler/app
    :port 8080
    :auto-reload? true
    :nrepl {:start? true}}
  :profiles
    {:uberjar {:omit-source true
               :env {:production true}
               :aot :all}

    :dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins [[lein-ring "0.10.0"]]}})
