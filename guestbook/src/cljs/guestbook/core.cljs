(ns guestbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn get-messages [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (vec %))}))

(defn send-message! [fields errors messages]
  (POST "/add-message"
        {:params @fields
         :headers
         {"Accept" "application/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :handler #(do
                     (reset! errors nil)
                     (swap! messages conj (assoc @fields :timestamp (js/Date.))))
         :error-handler #(do
                           (.log js/console (str %))
                           (reset! errors (get-in % [:response :errors])))}))

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (clojure.string/join error)]))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])

(defn message-form [messages]
  (let [fields (atom {})
        errors (atom nil)]
    (fn []
      [:div.content
       [:div.form-group
        [errors-component errors :name]
        [:p "Name:"
         [:input.form-control
          {:type :text
           :name :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value (:name @fields)}]]
        [errors-component errors :message]
        [:p "Message:"
         [:textarea.form-control
          {:rows 4
           :cols 50
           :name :message
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}
          (:message @fields)]]
        [:input.btn.btn-primary {:type :submit 
                                 :value "comment"
                                 :on-click #(send-message! fields errors messages)}]]])))

(defn home []
  (let [messages (atom nil)]
    (get-messages messages)
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list messages]]]
       [:div.row
        [:div.span12
         [message-form messages]]]])))

(reagent/render
  [home]
  (.getElementById js/document "content"))

; (-> (.getElementById js/document "content")
;     (.-innerHTML)
;     (set! "Hello World!"))

(.log js/console "HEYO!")
