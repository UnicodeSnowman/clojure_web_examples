(ns picture-gallery.util
  (:require [noir.session :as session])
  (:import java.io.File))

(def galleries "galleries")

(defn gallery-path []
  (str galleries File/separator (session/get :user)))
