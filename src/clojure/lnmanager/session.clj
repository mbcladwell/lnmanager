(ns lnmanager.session
  (:require [clojure.java.jdbc :as sql]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [codax.core :as c]
            [clojure.java.io :as io])
  (:import java.sql.DriverManager)
  (:gen-class ))

(load "/lnmanager/db")


(defn open-props-if-exists
  ;;1. check working directory - /home/user/my-working-dir
  ;;2. check home directory      /home/user
  ;;3. launch DialogPropertiesNotFound()
  []
  (if (.exists (io/as-file "ln-props"))
    (def props (c/open-database! "ln-props"))  
    (if (.exists (io/as-file (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (def props (c/open-database! (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (lnmanager.DialogPropertiesNotFound.))))

;;https://push-language.hampshire.edu/t/calling-clojure-code-from-java/865
;;
    
(defn setup-local-postgres-session []
(c/assoc-at! props [:conn] {:host "127.0.0.1"
	              :port "5432"
	              :sslmode "false"
	              :source "local"
                      :dbname "lndb"
                      :help-url-prefix "labsolns.com/software"
                      :password "welcome"
	              :user "ln-admin"	  
	              :temp-dir  (java.lang.System/getProperty "java.io.tmpdir")
	              :working-dir  (java.lang.System/getProperty "user.dir")
                      :home-dir  (java.lang.System/getProperty "user.home")}))
                                     
;;(c/get-at! props [:conn :port])

;;(c/update-at! props [:conn :source ]  {:source "dooby"})
;;(c/assoc-at! props [:conn :source ]  {:source "dooby"})


(defn set-ln-props [ path-to-db ]
    (def props (c/open-database! path-to-db))  )

(defn create-ln-props
  ;;
  [ host port sslmode user password]
  (def props (c/open-database! (str (java.lang.System/getProperty "user.dir") "/ln-props"))) ;

(c/assoc-at! props [:conn] {:host host
	              :port port
	              :sslmode sslmode
	              :source "local"
                      :dbname "lndb"
                      :help-url-prefix "labsolns.com/software"
                      :password password
	              :user user  
	              :temp-dir  (java.lang.System/getProperty "java.io.tmpdir")
	              :working-dir  (java.lang.System/getProperty "user.dir")
                            :home-dir  (java.lang.System/getProperty "user.home")}))

;;(create-ln-props "127.0.0.1" "5432" "false" "ln_admin" "welcome")

(defn get-all-props
  ;;note that the keys must be quoted for java
  []
  (into {} (java.util.HashMap.
           {":host" (c/get-at! props [:conn :host])
            ":port" (c/get-at! props [:conn :port])
           ":sslmode" (c/get-at! props [:conn :sslmode])
          ":source" (c/get-at! props [:conn :source])
          ":dbname" (c/get-at! props [:conn :dbname])
          ":help-url-prefix" (c/get-at! props [:conn :help-url-prefix])
          ":password" (c/get-at! props [:conn :password])
          ":user" (c/get-at! props [:conn :user])})))

(defn get-all-props-clj
  ;;a map for clojure
  [] 
  ({:host (c/get-at! props [:conn :host])
    :port (c/get-at! props [:conn :port])
    :sslmode (c/get-at! props [:conn :sslmode])
    :source (c/get-at! props [:conn :source])
    :dbname (c/get-at! props [:conn :dbname])
    :help-url-prefix (c/get-at! props [:conn :help-url-prefix])
    :password (c/get-at! props [:conn :password])
    :user (c/get-at! props [:conn :user])}))



(defn get-host []
   (c/get-at! props [:conn :host]))

(defn get-port []
  (:port (c/get-at! props [:conn :port])))

(defn get-source []
  (:source (c/get-at! props [:conn :source])))


;;(c/close-database! props)

(defn write-out-message [ int ]
  (* int 3)
  )

;;(write-out-message "my message")

(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (clojure.java.io/reader file-name)] 
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) (read-string v)])))))




(defn setup-heroku []
:host  "ec2-50-19-114-27.compute-1.amazonaws.com";
:port  "5432";
:sslmode  "require";	   
  :dbname  "d6dmueahka0hch";
  :help_url_prefix  "http://labsolns.com/software/";
		:password  "c5644d221fa636d8d8065d336014723f66df0c6b78e7a5390453c4a18c9b20b2";
		:user  "dpstpnuvjslqch";
		:URL  (str "jdbc:postgresql://"  :host  ":" :port "/" :dbname  "?sslmode=require&user=" :user "&password=" :password ))


(defn setup-elephant-sql []
		:host  "raja.db.elephantsql.com";
		:port  "5432";
		:sslmode  "require";	      
		:dbname  "klohymim";
		:source  "elephantsql";
		:help_url_prefix "http://labsolns.com/software/";
		:password  "hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_";
		:user  "klohymim";
	    	:URL  (str "jdbc:postgresql://"  :host  ":" :port  "/" :dbname  "?user=" :user  "&password=" :password  "&SSL=true" ))

	;;	(post-load-properties "elephantsql"))

(lnmanager.DialogPropertiesNotFound.)
