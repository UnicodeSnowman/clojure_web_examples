(ns guestbook.db.core
  (:require
    [yesql.core :refer [defqueries]]
    [config.core :refer [env]]))

(def conn
  {:classname "org.h2.Driver"
   :connection-uri (:database-url env)
   :make-pool? true
   :naming {:keys clojure.string/lower-case
            :fields clojure.string/upper-case}})

(defqueries "sql/queries.sql" {:connection conn})

; (get-messages)
; (save-message! {:name "Bob"
;                 :message "Hello World"
;                 :timestamp (java.util.Date.)})
