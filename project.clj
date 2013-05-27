(defproject moodseer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [
            [lein-ring "0.8.5"]
            ]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.2.0-SNAPSHOT"]
                 [hiccup "1.0.0"]
                 [enlive "1.1.1"]
                 [cheshire "5.2.0"]
                 ]
  :ring {:handler moodseer.routes/app}
  )
