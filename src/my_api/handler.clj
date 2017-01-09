(ns my-api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))


(s/defschema TxRequest
  {:txs [s/Any]})



(defn build-id-idx
  [read-model tx]
    (let [id (:db/id tx)]
      (reduce
        (fn [output [key value]]
          (update-in output [:db id] #(assoc % key value))) read-model tx)))


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
    {:db/id "8"
     :user/name "A dfghdfghfg name"
     :user/email "pw@gmail.com"
     :user/follows [{:$type "ref" :value [:user/id "1"]}]}))


(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "My-api"
                    :description "Event sourcing + falcor-style query example"}
             :tags [{:name "api", :description "some apis"}]}}}
    (GET "/healthcheck" [] "OK")
    (context "/api" []
      :tags ["api"]
      (GET "/query" []
        (ok (reduce build-id-idx {} @txs)))
      (POST "/command" [request]
        :body [txs TxRequest]
          (ok txs)))))
