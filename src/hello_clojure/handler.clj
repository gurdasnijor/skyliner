(ns hello-clojure.handler
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async :refer [<! >! <!! timeout chan alt! go]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hello-clojure.middleware :refer [wrap-logging wrap-request-id]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as resp]
            [hildebrand.core :as h]
            [cljs.core.match :refer-macros [match]]))


;The cache wil resemble the following shape
; {
;   :model {
;     :index_key {
;       "val1" {
;         model instance fields
;       }
;     }
;   }
;  :db {
;    ..a map of keys -> actual models (with all associated namespaced attrs)
;  }
; }

;;We can abuse ref $types to create a concept similar to
;;datomic's lookup refs:

;
; {
;   :user {
;     :email {
;       "joe@gmail.com" {
;         [{:$type "ref" :value [:user/id "0"] }]
;       }
;       "sally@gmail.com" {
;         [{:$type "ref" :value [:user/id "1"] }]
;       }
;     }
;   }
;
;   :db {
;     :id {
;       "0" {
;         :user/name "Joe"
;         :user/follows []
;         :user/email "joe@gmail.com"
;         :user/follows [{:$type "ref" :value [:user/id "1"] }]
;       }
;       "1" {
;         :user/name "Sally"
;         :user/follows []
;         :user/email "sally@gmail.com"
;         :follows []
;       }
;     }
;   }
; }
;



;
; (def init-events [
;   {:db/id "1"
;    :user/name "Sally"
;    :user/email "sally@gmail.com"
;    :user/follows []}
;    {:db/id "0"
;     :user/name "Joe"
;     :user/follows [{:$type "ref" :value [:user/id "1"]}]
;     :user/email "joe@gmail.com"}
;    {:db/id "0"
;     :user/otherAttr "someval"}
; ])


;
; (defn assoc-attr-to-entity [read-model attr-kv]
;   (let [key (first attr-kv)
;         value (second attr-kv)
;        [type-key attr-key] (map keyword (clojure.string/split (str key) #"/"))]
;    (update-in output [:db id] (fn [e] (assoc e :attr "val")))



(defn build-id-idx
  [read-model tx]
    (let [id (:db/id tx)]
      (reduce
        (fn [output [key value]]
          (update-in output [:db id] #(assoc % key value))) read-model tx)))


;build index on email... etc
; (group-by :user/email @txs)



; (def commands (atom []))
(def txs (atom [
  {:db/id "1"
   :user/name "Sally"
   :user/email "sally@gmail.com"
   :user/follows []}
   {:db/id "0"
    :user/name "Joe"
    :user/follows [{:$type "ref" :value [:user/id "1"]}]
    :user/email "joe@gmail.com"}
   {:db/id "0"
    :user/otherAttr "someval"}
]))



(swap! txs
  #(conj %
    {:db/id "6"
     :user/name "adsfasdf"
     :user/email "pw@gmail.com"
     :user/follows [{:$type "ref" :value [:user/id "1"]}]}))


(defn command-handler [request]
  (let [t (get-in request [:params "txs"])]
    (resp/response t)))


(defn index
  [request]
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/content-type "text/html")))

(defroutes app-routes
  (GET "/" [] index)
  (GET "/healthcheck" [] "OK")
  (GET "/query" [] (resp/response (reduce build-id-idx {} (take 2@txs))))
  (POST "/commands" [request]
    (let [t (get-in request [:params])]
      (resp/response {:test t})))
  (route/not-found "Not Found"))


(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-logging)
      (wrap-request-id)
      (wrap-json-params)))
