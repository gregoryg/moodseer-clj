(ns moodseer-clj.core
  (:use [noir.core]
        [hiccup.page]
        [net.cgrand.enlive-html :as html :only [deftemplate at content set-attr attr? strict-mode defsnippet]])
  (:require [noir.server :as server])
  (:require [moodseer-clj.ajax])
  (:require [cheshire.core :as json])
  (:import (java.io StringReader BufferedReader)))

(def starting-links [{:desc "Escape to the mothership" :url "http://www.google.com" :important true}
                     {:desc "First test page GORT" :url "/gort"}
                     {:desc "Moodseer HTML5 Clojure" :url "/zoneplayer" :important true}])

(defpage "/" []
  (html5 [:h1 "Hello World"]
         (include-css "/css/standard.css")
         [:h2 "First sub-section header"]
         [:ul
          (for [link starting-links]
          [:li [:a {:href (:url link)} (:desc link)]])
          ]))

(deftemplate index "html/gort.html"
  [ctxt]
  [:div#preamble] (html/content (:message ctxt))
  )

(defpartial layout [& content]
  (html5
   [:head
    [:title "My Moodseer Pang"]
    (include-css "/css/standard.css")]
   [:body
    [:div#wrapper
    content]]))

(deftemplate zone-player "html/tsplayer.html"
  [& payload]
  )

;; (deftemplate footer "html/footer.html" [& payload])

(defpage "/zoneplayer" []
  (zone-player))

(defpage "/gort" []
  (layout 
   (index {:message "That's the stuff"})
   [:p "Moodseer test..."]
   )
  )

;; (defpage "/api/upcoming" []
;;   (json/generate-string upcoming-test-list))

(defn -main [& [port]]
  (server/start (Integer. (or port "8080"))))

;;(defonce moodseer-server (server/start 8080))