(ns lnmanager.session
 ;; (:use [] :reload)

  (:require [clojure.java.jdbc :as sql]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [codax.core :as c]
            [clojure.java.io :as io])
  (:import java.sql.DriverManager javax.swing.JOptionPane)
  
  (:gen-class ))

(def props "")

  ;;(print-ap)

;; (println (c/get-at! props  [:assets :session 225 :user-group] ))
(defn read-props-text-file []
  (read-string (slurp "limsnucleus.properties")))

;;(read-props-text-file)

(defn set-ln-props [ path-to-db ]
    (def props (c/open-database! path-to-db))  )


(defn create-ln-props-from-text []
 (let [props (c/open-database! "ln-props")]
  (c/with-write-transaction [props tx]
    (-> tx
  (c/assoc-at [:assets ] (read-props-text-file))
  (c/assoc-at [:assets :session] {:project-id 0
	                          :project-sys-name ""
	                          :user-id 0	              
                                  :user-sys-name ""
                                  :plateset-id 0
                                  :plateset-sys-name ""
	                             :user-group-id 0
	                             :session-id 0
                                     :working-dir ""
                                  })))
 (c/close-database! props)))

;;(create-ln-props-from-text)
  ;;(print-ap)


(defn create-ln-props
  [ host port dbname source sslmode user password url target-dir]
  (def props (c/open-database! target-dir))
  (c/with-write-transaction [props tx]
    (-> tx
  (c/assoc-at [:assets :conn] {:host host
	                          :port port
	                          :sslmode sslmode	              
                                  :dbname dbname
                                  :source source
                                  :password password
	                          :user user
                                  :authenticated false
	                       :help-url-prefix "www.labsolns.com/software"
                               })
  (c/assoc-at [:assets :session] {:project-id 0
	                          :project-sys-name ""
	                          :user-id 0	              
                                  :user-sys-name ""
                                  :plateset-id 0
                                  :plateset-sys-name ""
	                             :user-group-id 0
	                             :session-id 0
                                     :working-dir ""
                                     })
    )))

;;(create-ln-props "127.0.0.1" "5432" "lndb" "local" "false" "ln_admin" "welcome" "www.labsolns.com/software" (str (java.lang.System/getProperty "user.dir") "/ln-props") )

(defn get-host []
   (c/get-at! props [:assets :conn :host]))

(defn get-port []
  (c/get-at! props [:assets :conn :port]))

(defn get-source []
  (c/get-at! props [:assets :conn :source]))

(defn get-dbname []
  (c/get-at! props [:assets :conn :dbname]))

(defn get-user []
  (c/get-at! props [:assets :conn :user]))

(defn get-password []
  (c/get-at! props [:assets :conn :password]))



(defn  get-connection-string [target]	  
  (case target
  	"heroku" (str "jdbc:postgresql://"  (get-host) ":" (get-port)  "/" (get-dbname) "?sslmode=require&user=" (get-user) "&password="  (get-password))
	  "local" (str "jdbc:postgresql://" (get-host) "/" (get-dbname))	   
	  "internal" (str "jdbc:postgresql://" (get-host) "/" (get-dbname))	   
	  "elephantsql" (str "jdbc:postgresql://" (get-host) ":" (get-port) "/" (get-dbname) "?user=" (get-user) "&password=" (get-password) "&SSL=true" )
          "test" (str "jdbc:postgresql://" (get-host) ":" (get-port) "/" (get-dbname) "?user=" (get-user) "&password=" (get-password) "&SSL=true" ) ))

;;(get-connection-string "heroku")

;;https://push-language.hampshire.edu/t/calling-clojure-code-from-java/865
;;(open-props-if-exists)

;;psql -U ln_admin -h 192.168.1.11 -d lndb

(defn get-all-props
  ;;note that the keys must be quoted for java
  []
  (into {} (java.util.HashMap.
           {":host" (c/get-at! props [:assets :conn :host])
            ":port" (c/get-at! props [:assets :conn :port])
           ":sslmode" (c/get-at! props [:assets :conn :sslmode])
          ":source" (c/get-at! props [:assets :conn :source])
          ":dbname" (c/get-at! props [:assets :conn :dbname])
          ":help-url-prefix" (c/get-at! props [:assets :conn :help-url-prefix])
          ":password" (c/get-at! props [:assets :conn :password])
          ":user" (c/get-at! props [:assets :conn :user])})))

;; (get-all-props)

(defn get-all-props-clj
  ;;a map for clojure
  [] 
  ({:host (c/get-at! props [:assets :conn :host])
    :port (c/get-at! props [:assets :conn :port])
    :sslmode (c/get-at! props [:assets :conn :sslmode])
    :source (c/get-at! props [:assets :conn :source])
    :dbname (c/get-at! props [:assets :conn :dbname])
    :help-url-prefix (c/get-at! props [:assets :conn :help-url-prefix])
    :password (c/get-at! props [:assets :conn :password])
    :user (c/get-at! props [:assets :conn :user])}))



  (defn print-ap 
    "This version prints everything"
    []
    (println (str "Whole map: " (c/get-at! props []) )))

  ;;(print-ap)
  ;;(print-all-props)
  ;;(println props)
;;(c/close-database! props)



    (defn get-help-url-prefix []
        (c/with-write-transaction [props tx]
          (c/assoc-at tx [:assets :props :help-url-prefix ])))



      (defn set-authenticated [b]
          (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :authenticated] b)))

(defn get-authenticated []
  (c/get-at! props [:assets :session :authenticated ]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (not (.exists (io/as-file "ln-props")))
    
    (if (not (.exists (io/as-file (str (java.lang.System/getProperty "user.dir") "/limsnucleus.properties") )))
      (JOptionPane/showMessageDialog nil "limsnucleus.properties file is missing!"  )
      (create-ln-props-from-text))

    )  
  (def props (c/open-database! "ln-props"))
  (lnmanager.DialogPropertiesNotFound. (get-all-props) )
  )
   

;;(-main)

;;(c/close-database! props)

