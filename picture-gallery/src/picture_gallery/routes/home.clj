(ns picture-gallery.routes.home
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [picture-gallery.views.layout :as layout]))

(defn home []
  (layout/common [:h1 "Hello " (session/get :user)]))

(defroutes home-routes
  (GET "/" [] (home)))
