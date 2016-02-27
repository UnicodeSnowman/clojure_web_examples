(ns guestbook.routes.ws
  (:require [compojure.core :refer [GET defroutes]]
            [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [guestbook.db.core :as db]))

(defonce channels (atom #{}))

(defn connect! [channel]
  (log/info "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "close code:" code "reason: " reason)
  (swap! channels #(remove #{channel} %)))
