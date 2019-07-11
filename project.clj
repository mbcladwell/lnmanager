(defproject lnmanager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure/"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cider/cider-nrepl "0.11.0-SNAPSHOT"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [java-jdbc/dsl "0.1.3" ]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [honeysql "0.9.1"]
                 [org.clojure/data.csv "0.1.4"]
                 [incanter/incanter-core "1.9.1"]
                 [javax.help/javahelp "2.0.05"]
                 [javax.swing/jlfgr "1.0"]
                 [leinjacker "0.4.2"]
                 [lein-codox "0.10.3"]
                 [codax "1.3.1"]
                 [org.postgresql/postgresql "42.2.5"]]


:repl-options {:nrepl-middleware
                 [cider.nrepl.middleware.apropos/wrap-apropos
                  cider.nrepl.middleware.classpath/wrap-classpath
                  cider.nrepl.middleware.complete/wrap-complete
                  cider.nrepl.middleware.info/wrap-info
                  cider.nrepl.middleware.inspect/wrap-inspect
                  cider.nrepl.middleware.macroexpand/wrap-macroexpand
                  cider.nrepl.middleware.ns/wrap-ns
                  cider.nrepl.middleware.resource/wrap-resource
                  cider.nrepl.middleware.stacktrace/wrap-stacktrace
                  cider.nrepl.middleware.test/wrap-test
                  cider.nrepl.middleware.trace/wrap-trace
                  cider.nrepl.middleware.undef/wrap-undef]}




  :main ^:skip-aot lnmanager.core
  :aot [ ]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
