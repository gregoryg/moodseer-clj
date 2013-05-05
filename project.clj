(defproject moodseer-clj "0.0.1"
  :description "Write a browser front end to the Moodseer streaming audio server"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/java.jdbc "0.1.1"]
		 [mysql/mysql-connector-java "5.1.17"]
                 [compojure "1.2.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [ring-server "0.2.7"]
                 [lib-noir "0.2.0"]
		 [enlive "1.1.1"]
                 [org.clojure/tools.trace "0.7.1"]
		 ]
  :plugins      [[lein-swank "1.4.3"]
                 [lein-ring "0.8.2"]
                 [lein-ritz "0.7.0"]]
  :ring {:handler moodseer-clj/app-routes}
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [org.clojure/java.jdbc "0.1.1"]]
  )
