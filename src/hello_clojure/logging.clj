(ns hello-clojure.logging
  (:require [clj-time.format :as tf]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [unilog.config :as unilog])
  (:import [ch.qos.logback.classic Level Logger]
           [net.logstash.logback.marker MapEntriesAppendingMarker]
           [org.slf4j LoggerFactory Marker MDC]
           [org.joda.time DateTime]))

(defn start-logging!
  "Initializes the logging subsystems to log all statements info or higher to
  STDOUT as JSON objects."
  []
  (unilog/start-logging! {:console {:encoder :json} :level :info}))

(defn stop-logging!
  "Disables all logging."
  []
  (unilog/start-logging! {:console false, :level :off}))

(defn- datetime->str
  "Converts all datetimes to datetime-lookin' strings."
  [m]
  (walk/postwalk #(if (instance? DateTime %)
                    (tf/unparse (tf/formatters :date-time) %)
                    %) m))

(defn map->marker
  "Convert a map of data to an SLF4J marker, allowing us to add arbitrary JSON
  values to log entries."
  [m]
  (->> m
       (walk/postwalk #(if (keyword? %) (name %) %))
       datetime->str
       (into (sorted-map))
       (MapEntriesAppendingMarker.)))

(defmacro log
  "Log the given message, optional data, and optional exception at the given
  level."
  ([{:keys [level msg data throwable]}]
   (let [llevel (->> level name string/upper-case
                     (str "ch.qos.logback.classic.Level/")
                     symbol)]
     `(let [^Logger logger# (LoggerFactory/getLogger (str ~*ns*))]
        (when (.isGreaterOrEqual ~llevel (.getEffectiveLevel logger#))
          (.log logger#
                (map->marker ~data)
                (str ~*ns*)
                (Level/toLocationAwareLoggerInteger ~llevel)
                ~msg nil ~throwable)))))
  ([level msg]
   `(log {:level ~level :msg ~msg}))
  ([level msg data]
   `(log {:level ~level :msg ~msg :data ~data}))
  ([level msg data throwable]
   `(log {:level ~level :msg ~msg :data ~data :throwable ~throwable})))
