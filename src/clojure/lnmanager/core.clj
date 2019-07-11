(ns lnmanager.core
  (:require [lnmanager.session])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (lnmanager.session/open-props-if-exists))


