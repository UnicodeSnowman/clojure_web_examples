(ns liberator-service.routes.home
  (:require [compojure.core :refer :all]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [liberator.core
             :refer [defresource resource request-method-in]]
            [liberator-service.views.layout :as layout]))

(def users (atom ["John" "Jane"]))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :malformed? (fn [context]
                (let [params (get-in context [:request :form-params])]
                  (empty? (get params "user"))))
  :handle-malformed "user name cannot be empty!"
  :post!
  (fn [context]
    (let [params (get-in context [:request :form-params])]
      (swap! users conj (get params "user"))))
  :handle-created (fn [_] (generate-string @users))
  :available-media-types ["application/json"])

(defn home []
  (layout/common [:h1 "Hello World!"]))

; return 503
; (defresource not-available
;   :service-available? false
;   :handle-ok "Hello World!"
;   :etag "fixed-etag"
;   :available-media-types ["text/plain"])

; (defresource home
;   :handle-ok "Hello World!"
;   :etag "fixed-etag"
;   :available-media-types ["text/plain"])

; restrict allowed method
; (defresource home
;   :method-allowed?
;   (fn [context]
;     (= :get (get-in context [:request :request-method])))
;   :handle-ok "Hello World!"
;   :etag "fixed-etag"
;   :available-media-types ["text/plain"])

; or, baked in to the framework...
; (defresource home
;   :allowed-methods [:get]
;   :handle-ok "Hello World!"
;   :etag "fixed-etag"
;   :available-media-types ["text/plain"])

; handle not allowed method
; (defresource home
;   :service-available? true
;   :method-allowed? (request-method-in :get)
;   :handle-service-not-available "service is currently unavailable..."
;   :handle-method-not-allowed
;   (fn [context]
;     (str (get-in context [:request :request-method]) " is not allowed"))
;   :handle-ok "Hello World!"
;   :etag "fixed-etag"
;   :available-media-types ["text/plain"])

(defresource home
  :available-media-types ["text/html"]
  :exists?
  (fn [context]
    [(io/get-resource "/home.html")
     {::file (file (str (io/resource-path) "/home.html"))}])
  :handle-ok
  (fn [{{{resource :resource} :route-params} :request}]
    (clojure.java.io/input-stream (io/get-resource "/home.html")))
  :last-modified
  (fn [{{{resource :resource} :route-params} :request}]
    (.lastModified (file (str (io/resource-path) "/home.html")))))

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))

; or

; (defroutes home-routes
;   (ANY "/" request
;        (resource
;          :handle-ok "Hello World!"
;          :etag "fixed-etag"
;          :available-media-types ["text/plain"])))
