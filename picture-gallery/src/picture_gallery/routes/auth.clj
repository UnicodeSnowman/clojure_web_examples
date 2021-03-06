(ns picture-gallery.routes.auth
  (:require [hiccup.form :refer :all]
            [compojure.core :refer :all]
            [picture-gallery.models.db :as db]
            [picture-gallery.routes.home :refer :all]
            [picture-gallery.util :refer [gallery-path]]
            [picture-gallery.views.layout :as layout]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.validation :as validation]
            [noir.response :as resp])
  (:import java.io.File))

(defn valid? [id pass pass1]
  (validation/rule (validation/has-value? id)
                   [:id "user id is required"])
  (validation/rule (validation/min-length? pass 5)
                   [:pass "password must be at least 5 characters"])
  (validation/rule (= pass pass1)
                   [:pass "entered passwords do not match"])
  (not (validation/errors? :id :pass :pass1)))

(defn error-item [[error]]
  [:div.error error])

(defn control [id label field]
  (list
    (validation/on-error id error-item)
    label field
    [:br]))

(defn registration-page [& [id]]
  (layout/base
    (form-to [:post "/register"]
             (control
                :id
                (label "user-id" "user id")
                (text-field {:tabindex 1} "id" id))
              (control
                :pass
                (label "pass" "password")
                (password-field {:tabindex 2} "pass"))
              (control
                :pass1
                (label "pass1" "retype password")
                (password-field {:tabindex 3} "pass1"))
             (submit-button {:tabindex 4} "create account"))))

(defn format-error [id ex]
  (cond
    (and
      (instance? org.postgresql.util.PSQLException ex)
      (= 0 (.getErrorCode ex)))
      (str "The user with id " id " already exists!")
    :else
      "An error has occurred while processing the request"))

(defn handle-registration [id pass pass1]
  (if (valid? id pass pass1)
    (try
      (db/create-user {:id id :pass (crypt/encrypt pass)})
      (session/put! :user id)
      (create-gallery-path)
      (resp/redirect "/")
      (catch Exception ex
        (validation/rule false [:id (format-error id ex)])
        (registration-page)))
    (registration-page id)))

(defn create-gallery-path []
  (let [user-path (File. (gallery-path))]
    (if-not (.exists user-path) (.mkdirs user-path))
    (str (.getAbsolutePath user-path) File/separator)))

(defn handle-login [id pass]
  (let [user (db/get-user id)]
    (if (and
          user
          (crypt/compare pass (:pass user)))
      (session/put! :user id)))
  (resp/redirect "/"))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

(defroutes auth-routes
  (POST "/login" [id pass] (handle-login id pass))
  (GET "/logout" [] (handle-logout))
  (GET "/register" [] (registration-page))
  (POST "/register" [id pass pass1]
        (handle-registration id pass pass1)))
