(ns hello-clojure.middleware
  (:require [hello-clojure.logging :refer [log]]
            [ring.util.response :as response]
            [unilog.context :as mdc])
  (:import [java.util UUID]))

(defn wrap-request-id
  "Gives each request a unique ID and includes the ID in the response headers.

  If the incoming request has a trace ID from an Application Load Balancer, that
  is used. Otherwise, a UUID is generated."
  [handler]
  (fn [req]
    (let [request-id (or (response/get-header req "X-Amzn-Trace-Id")
                         (str (UUID/randomUUID)))]
      (-> req
          (assoc :request-id request-id)
          handler
          (response/header "Request-ID" request-id)))))

(defn- request-context
  "Returns a logging context for the given request, which is simply all the
  given request fields except for the headers and body."
  [req]
  (-> req
      (dissoc :body :cookies :form-params :headers :multipart-params :flash
              :query-params :session/key :session-id :query-string :request-id)
      (update :session dissoc :ring.middleware.anti-forgery/anti-forgery-token)))

(defn wrap-logging
  "Wraps the given Ring handler with logging. Adds a unique `request-id` field
  to both the MDC context and the incoming request."
  [handler]
  (fn [req]
    (let [request-id (:request-id req)
          start-time (System/currentTimeMillis)]
      (mdc/with-context {:request-id request-id}
        (let [resp     (handler req)
              end-time (System/currentTimeMillis)
              data     (merge (request-context req)
                              {:duration (- end-time start-time)
                               :status   (:status resp)})]
          (log :info "request handled" data)
          resp)))))
