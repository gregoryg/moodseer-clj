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

;; (defpartial api [params & [content]]
;;   (let [params (into {} params)
;;         cmd (:cmd params)
;;         resp
;;         (cond
;;           (= "play" cmd) (moodseer-clj.process/start-player "-shuffle -playlist /home/gregj/work/all.m3u")
;;           (= "pause" cmd) (moodseer-clj.process/pause-player)
;;           (= "stop" cmd) (moodseer-clj.process/pause-player)
;;           (= "next" cmd) (moodseer-clj.process/next-player)
;;           (= "prev" cmd) (moodseer-clj.process/prev-player)
;;           (= "quit" cmd) (moodseer-clj.process/quit-player)
;;           (= "nowplaying" cmd) (moodseer-clj.process/mplayer-pairs->map (moodseer-clj.process/get-player-track-info)))]
;;     (if (= "nowplaying" cmd)
;;       (cheshire.core/generate-string {:status true :message "Hello" :nowplaying resp})
;;       (cheshire.core/generate-string {:testresponse {:message "WTF honey" :muted false :command  cmd}}))))

(defpartial api [params & [content]]
  (let [params (into {} params)
        cmd (:cmd params)
        resp
        (cond
          (= "play" cmd) (moodseer-clj.process/play-player)
          (= "pause" cmd) (moodseer-clj.process/pause-player)
          (= "stop" cmd) (moodseer-clj.process/pause-player)
          (= "next" cmd) (moodseer-clj.process/next-player)
          (= "prev" cmd) (moodseer-clj.process/prev-player)
          (= "quit" cmd) nil
          (= "nowplaying" cmd) (moodseer-clj.process/player-nowplaying)
          (= "upcoming"   cmd) (moodseer-clj.process/get-player-upcoming))
        ]
    (cond
      (= "nowplaying" cmd) (cheshire.core/generate-string {:status true :message "Hello" :nowplaying resp})
      (= "upcoming" cmd) (cheshire.core/generate-string {:status true :message "Hello" :upcoming {:songlist resp}})
      true      (cheshire.core/generate-string {:testresponse {:message "WTF honey" :muted false :command  cmd}}))))

(defpage "/api:cmd" {:as params}
  (api params))

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


(defpage "/newville" []
  (newville))

(deftemplate newville "html/newplayer.html" 
  [& payload]
  )

;; (defpage "/api/upcoming" []
;;   (json/generate-string upcoming-test-list))

(defn -main [& [port]]
  (server/start (Integer. (or port "8080"))))

;;(defonce moodseer-server (server/start 8080))