(ns guestbook.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [guestbook.views.layout :as layout]
            [guestbook.models.db :as db]
            [noir.session :as session]
            [noir.response :refer [redirect]]
            [noir.util.crypt :as crypt]
            [noir.validation
              :refer [rule errors? has-value? on-error]]
            [hiccup.form :refer 
             [form-to label text-field password-field submit-button]]))

(defn format-error [[error]]
  [:p.error error])

(defn control [field name text]
  (list (on-error name format-error)
        (label name text)
        (field name)
        [:br]))

(defn registration-page []
  (layout/common
    (form-to [:post "/register"]
             (control text-field :id "screen name")
             (control password-field :pass "Password")
             (control password-field :pass1 "Retype Password")
             (submit-button "create account"))))

(defn login-page []
  (layout/common
    (form-to [:post "/login"]
             (control text-field :id "Screen Name")
             (control password-field :pass "Password")
             (submit-button "login"))))

(defn handle-login [id pass]
  (let [user (db/get-user id)]
    (rule (has-value? id)
      [:id "screen name is required"])
    (rule (= id "foo")
      [:id "unknown user"])
    (rule (has-value? pass)
      [:pass "password is required"])
    (rule (= pass "bar")
      [:pass "invalid password"])

    (if (errors? :id :pass)
      (login-page)
      (do
        (session/put! :user id)
        (redirect "/")))))

(defn handle-registration [id pass pass1]
  (rule (= pass pass1)
        [:pass "password was not retyped correctly"])
  (if (errors? :pass)
    (registration-page)
    (do
      (db/add-user-record {:id id :pass (crypt/encrypt pass)})
      (redirect "/login"))))

(defroutes auth-routes
  (GET "/login" [_] (login-page))
  (POST "/login" [id pass]
        (handle-login id pass))
  (GET "/logout" [_]
       (layout/common
         (form-to [:post "/logout"]
                  (submit-button "logout"))))
  (POST "/logout" [_]
        (session/clear!)
        (redirect "/"))
  (GET "/register" [_] (registration-page))
  (POST "/register" [id pass pass1]
        (handle-registration id pass pass1)))
