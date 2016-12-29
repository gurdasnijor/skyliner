(ns hello-clojure.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hello-clojure.middleware :refer [wrap-logging wrap-request-id]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as resp]))

(defn index
  [request]
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/content-type "text/html")))

(defroutes app-routes
  (GET "/" [] index)
  (GET "/healthcheck" [] "OK")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-logging)
      (wrap-request-id)))
