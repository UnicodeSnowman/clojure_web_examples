(ns swagger-service.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[swagger-service started successfully]=-"))
   :middleware identity})
