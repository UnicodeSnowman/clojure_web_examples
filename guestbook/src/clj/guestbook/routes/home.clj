(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response status]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [guestbook.db.core :as db]))

;(defn home-page []
;  (layout/render
;    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> {:errors errors} response (status 400))
    (do
      (db/save-message!
        (assoc params :timestamp (java.util.Date.)))
      (response {:status :ok}))))

;(defn home-page [{:keys [flash]}]
;  (layout/render
;    "home.html"
;    (merge {:messages (db/get-messages)}
;           (select-keys flash [:name :message :errors]))))

(defn home-page []
  (layout/render "home.html"))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/messages" [] (response (db/get-messages)))
  (POST "/add-message" request (save-message! request))
  (GET "/about" [] (about-page)))
