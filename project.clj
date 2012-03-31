(defproject moodseer-clj "0.0.1"
  :description "Write a browser front end to the Moodseer streaming audio server"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/java.jdbc "0.1.1"]
		 [mysql/mysql-connector-java "5.1.17"]
		 [clj-json "0.5.0"]
		 [noir "1.3.0-beta2"]
		 [enlive "1.0.0-SNAPSHOT"]
		 ]
  :plugins      [[lein-swank "1.4.3"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [org.clojure/java.jdbc "0.1.1"]]
  )