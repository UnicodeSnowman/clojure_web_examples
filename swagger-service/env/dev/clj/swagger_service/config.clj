(ns swagger-service.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [swagger-service.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[swagger-service started successfully using the development profile]=-"))
   :middleware wrap-dev})
