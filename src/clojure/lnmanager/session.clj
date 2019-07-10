(ns lnmanager.session
  (:require [clojure.java.jdbc :as sql]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [codax.core :as c]
            [clojure.java.io :as io])
  (:import java.sql.DriverManager)
  (:gen-class))


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


(defn get-host []
   (c/get-at! props [:conn :host]))

(defn get-port []
  (:port (c/get-at! props [:conn :port])))

(defn get-source []
  (:source (c/get-at! props [:conn :source])))


;;(c/close-database! props)



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

(defn -getlaspng
  ;;get the logo
  []
  (io/resource "images/las.png"))
