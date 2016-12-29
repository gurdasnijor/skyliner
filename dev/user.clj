(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [hello-clojure.main :as main]))

(defn start
  []
  (main/start-server!))

(defn stop
  []
  (main/stop-server!))

(defn go
  []
  (start)
  :ready)

(defn reset
  []
  (stop)
  (tn/refresh :after 'user/go))
