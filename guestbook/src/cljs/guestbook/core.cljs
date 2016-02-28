(ns guestbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]
            [guestbook.ws :as ws]))

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

(defn message-form [fields errors]
  [:div.content
   [:div.form-group
    [errors-component errors :name]
    [:p "Name:"
     [:input.form-control
      {:type :text
       :on-change #(swap! fields assoc :name (-> % .-target .-value))
       :value (:name @fields)}]]
    [errors-component errors :message]
    [:p "Message:"
     [:textarea.form-control
      {:rows 4
       :cols 50
       :value (:message @fields)
       :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
    [:input.btn.btn-primary {:type :submit 
                             :value "comment"
                             :on-click #(ws/send-message! @fields)}]]])

(defn response-handler [messages fields errors]
  (fn [message]
    (if-let [response-errors (:errors message)]
      (reset! errors response-errors)
      (do
        (reset! errors nil)
        (reset! fields nil)
        (swap! messages conj message)))))

(defn home []
  (let [messages (atom nil)
        errors   (atom nil)
        fields   (atom nil)]
    (ws/connect! (str "ws://" (.-host js/location) "/ws")
                 (response-handler messages fields errors))
    (get-messages messages)
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list messages]]]
       [:div.row
        [:div.span12
         [message-form fields errors]]]])))

(reagent/render
  [home]
  (.getElementById js/document "content"))

; (-> (.getElementById js/document "content")
;     (.-innerHTML)
;     (set! "Hello World!"))
