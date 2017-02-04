(ns my-api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [datomic.api :as d]))


(s/defschema TxRequest
  {:txs [s/Any]})


(defn some-stuf []
  (map count '([])))

  (d/q '[:find ?first
         :where [_ :firstName ?first]]
     [[1 :firstName "John" 42]
      [1 :lastName "Doe" 42]])


 

  (def init-data
    {:dashboard/items
     [{:id 0 :type :dashboard/post
       :author "Laura Smith"
       :title "A Post!"
       :content "Lorem ipsum dolor sit amet, quem atomorum te quo"}
      {:id 1 :type :dashboard/photo
       :title "A Photo!"
       :image "photo.jpg"
       :caption "Lorem ipsum"}
      {:id 2 :type :dashboard/post
       :author "Jim Jacobs"
       :title "Another Post!"
       :content "Lorem ipsum dolor sit amet, quem atomorum te quo"}
      {:id 3 :type :dashboard/graphic
       :title "Charts and Stufff!"
       :image "chart.jpg"}
      {:id 4 :type :dashboard/post
       :author "May Fields"
       :title "Yet Another Post!"
       :content "Lorem ipsum dolor sit amet, quem atomorum te quo"}]})



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
     :user/name "b"
     :user/email "pw@gmail.com"
     :user/follows [{:$type "ref" :value [:user/id "1"]}]}))


(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "My-api"
                    :description "Basic API example"}
             :tags [{:name "api", :description "some apis"}]}}}
    (GET "/healthcheck" [] "OK")
    (context "/api" []
      :tags ["api"]
      (GET "/ogql" [request]
        :query-params [query :- String]
        (prn (read-string query))
        (prn "asdfasdfasdf")
        (ok (reduce build-id-idx {} @txs)))
      (POST "/command" [request]
        :body [txs TxRequest]
          (ok txs)))))



;http://0.0.0.0/api/ogql?query=[[:db%208]]

; localhost:8080/api/ogql?query=[[:db 8]]