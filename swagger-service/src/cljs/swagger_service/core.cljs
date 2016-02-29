(ns swagger-service.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]])
  (:require-macros [secretary.core :refer [defroute]]))

(defn fetch-links! [links link-count]
  (GET "/api/cat-links"
       {:params {:link-count link-count}
        :handler #(reset! links (vec (partition-all 3 %)))}))

(defn images [links page]
  [:div.text-xs-center
   (for [row links]
     ^{:key row}
     [:div.row
      (for [link row]
        ^{:key link}
        [:div.col-sm-4
         [:img {:width 200 :src link}]])])])

(defn home-page []
  (let [links (atom [])
        page (atom 0)]
    (fetch-links! links 20)
    (fn []
      [:div
       (when @links
         [images @links @page])])))

(defn mount-components []
  (reagent/render-component [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))
