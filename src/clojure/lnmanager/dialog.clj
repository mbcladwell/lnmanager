(ns lnmanager.dialog
   (:use [seesaw core table dev mig border])

  (:require [clojure.java.io :as io]
            [clojure.string ] )
  (:import [javax.swing JFileChooser JEditorPane JFrame JScrollPane BorderFactory AbstractButton]
           java.awt.Font java.awt.Toolkit )
  (:import [java.net.URL])
  (:gen-class))

                               

(defn login-dialog
  ;;
  []
 (->  (frame :title "Login to LIMS*Nucleus"
             :on-close :exit
             :content "Some Content")
      pack!
      show!))

(login-dialog)
