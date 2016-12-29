(ns hello-clojure.main
  (:gen-class)
  (:require [clj-time.core :as t]
            [hello-clojure.handler :as handler]
            [hello-clojure.logging :as logging]
            [ring.adapter.jetty :as jetty])
  (:import [org.eclipse.jetty.server Server]
           [org.eclipse.jetty.server.handler StatisticsHandler]))

(def ^:private server (atom nil))

(defn- configure-server
  [^Server server]
  (let [stats-handler (StatisticsHandler.)
        default-handler (.getHandler server)]
    (.setHandler stats-handler default-handler)
    (.setHandler server stats-handler))
  (.setStopAtShutdown server true)
  (.setStopTimeout server 1000))

(defn make-server
  [join?]
  (jetty/run-jetty handler/app
                   {:configurator         configure-server
                    :port                 8080
                    :join?                join?
                    :daemon?              false
                    :http?                true
                    :ssl?                 false
                    :max-threads          50
                    :min-threads          8
                    :max-idle-time        (t/in-millis (t/seconds 30))
                    :send-date-header?    true
                    :output-buffer-size   32768
                    :request-header-size  8192
                    :response-header-size 8192
                    :send-server-version? false}))

(defn start-server!
  [& join?]
  (logging/start-logging!)
  (when-not @server
    (reset! server (make-server (boolean join?)))))

(defn stop-server!
  []
  (logging/stop-logging!)
  (when-let [s @server]
    (.stop ^Server s))
  (reset! server nil))

(defn -main
  [& args]
  (start-server! true))
