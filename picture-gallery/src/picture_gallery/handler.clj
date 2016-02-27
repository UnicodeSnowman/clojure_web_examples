(ns picture-gallery.handler
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes]]
            [noir.util.middleware :as noir-middleware]
            [noir.session :as session]
            [picture-gallery.routes.home :refer [home-routes]]
            [picture-gallery.routes.auth :refer [auth-routes]]
            [picture-gallery.routes.upload :refer [upload-routes]]))

(defn init []
  (println "picture-gallery is starting"))

(defn destroy []
  (println "picture-gallery is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn user-page [_]
  (session/get :user))

;(def app
;  (-> (routes home-routes app-routes)
;      (handler/site)
;      (wrap-base-url)))

(def app
  (noir-middleware/app-handler [auth-routes
                                home-routes
                                upload-routes
                                app-routes]
                               :access-rules [user-page]))
