(ns swagger-service.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.xml :as xml]))

(s/defschema Thingie {:id Long
                      :hot Boolean
                      :tag (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(defn parse-xml [xml]
  (-> xml .getBytes io/input-stream xml/parse))

(defn get-links [link-count]
  (->
    "http://thecatapi.com/api/images/get?format=xml&results_per_page="
    (str link-count)
    client/get
    :body
    parse-xml))

;(clojure.pprint/pprint (get-links 3))
;(def result (get-links 3))
;(clojure.pprint/pprint result)

(defn get-first-child [tag xml-node]
  (->>
    xml-node
    :content
    (filter (fn [item] (= (:tag item) tag)))
    first))

(defn parse-link [link]
  (->>
    link
    (get-first-child :url)
    :content
    first))

(defn parse-links [links]
  (->>
    links
    (get-first-child :data)
    (get-first-child :images)
    :content
    (map parse-link)))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
    :tags ["thingie"]

    (GET "/cat-links" []
         :query-params [link-count :- Long]
         :summary "returns a collection of image links"
         :return [s/Str]
         (ok (-> 
               (get-links link-count)
               (parse-links))))

    (GET "/plus" []
      :return       Long
      :query-params [x :- Long, {y :- Long 1}]
      :summary      "x+y with query-parameters. y defaults to 1."
      (ok (+ x y)))

    (POST "/minus" []
      :return      Long
      :body-params [x :- Long, y :- Long]
      :summary     "x-y with body-parameters."
      (ok (- x y)))

    (GET "/times/:x/:y" []
      :return      Long
      :path-params [x :- Long, y :- Long]
      :summary     "x*y with path-parameters"
      (ok (* x y)))

    (POST "/divide" []
      :return      Double
      :form-params [x :- Long, y :- Long]
      :summary     "x/y with form-parameters"
      (ok (/ x y)))

    (GET "/power" []
      :return      Long
      :header-params [x :- Long, y :- Long]
      :summary     "x^y with header-parameters"
      (ok (long (Math/pow x y))))

    (PUT "/echo" []
      :return   [{:hot Boolean}]
      :body     [body [{:hot Boolean}]]
      :summary  "echoes a vector of anonymous hotties"
      (ok body))

    (POST "/echo" []
      :return   (s/maybe Thingie)
      :body     [thingie (s/maybe Thingie)]
      :summary  "echoes a Thingie from json-body"
      (ok thingie)))

  (context "/context" []
    :tags ["context"]
    :summary "summary inherited from context"
    (context "/:kikka" []
      :path-params [kikka :- s/Str]
      :query-params [kukka :- s/Str]
      (GET "/:kakka" []
        :path-params [kakka :- s/Str]
        (ok {:kikka kikka
             :kukka kukka
             :kakka kakka})))))

(def a (vec (partition-all 6 [1 2 3 4 5 6 7 8 8 8 8 8 8])))
(for [row (partition-all 3 a)]
  (prn row))
