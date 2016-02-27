(ns guestbook.routes.home
  (:require [compojure.core :refer :all]
            [guestbook.views.layout :as layout]
            [noir.session :as session]
            [hiccup.form :refer :all]
            [guestbook.models.db :as db]))

(defn format-time [timestamp]
  (-> "dd/MM/yyyy"
      (java.text.SimpleDateFormat.)
      (.format timestamp)))

(defn show-guests []
  [:ul.guests
    (for [{:keys [message name timestamp]} (db/read-guests)]
    [:li
      [:blockquote message]
      [:p "-" [:cite name]]
      [:time (format-time timestamp)]])])

; generate HTML
; (hiccup.core/html (show-guests))
; (hiccup.core/html [:a {:href "https://www.google.com"} "Google"])

(defn home [& [name message error]]
  (layout/common
    [:h1 "Guestbook " (session/get :user)]
    [:p "Welcome to my guestbook"]
    [:p error]
    (show-guests)
    [:hr]
    (form-to [:post "/"]
      [:p "Name:"]
      (text-field "name" name)
      [:p "Message:"]
      (text-area {:rows 10 :cols 40} "message" message)
      [:br]
      (submit-button "comment"))))

(defn save-message [name message]
  (cond
    (empty? name)
    (home name message "Some dummy forgot to leave a name")
    (empty? message)
    (home name message "Don't you have something to say?")
    :else
    (do
      (db/save-message name message)
      (home))))

(defn example [id & other]
  (prn other)
  (layout/common
    [:h1 "Example"]
    [:div id]))
    ;[:div (:remote-addr request-map)]))

(defn tester [value & other]
  (prn other)
  (layout/common
    [:h1 (str "Tester " value)]))

(defroutes home-routes
  (GET "/records" []
       (noir.response/content-type "text/plain" "some plain text"))
  (GET "/get-message" []
       (noir.response/json {:message "everything is cool!"}))
  (GET "/" [] (home))
  (GET "/foo" request (interpose ", " (keys request)))
  (GET "/example/:id" [id] (example id))
  (context "/user/:id" [id]
       (GET "/profile" {params :route-params} (tester "profile" params))
       (GET "/settings" [] (tester "settings")))
  (POST "/" [name message] (save-message name message)))
