(ns moodseer.views
  (:use 
   moodseer.process
   [hiccup core page]
        [net.cgrand.enlive-html :as html :only [deftemplate at content set-attr attr? strict-mode defsnippet]]
        )
  (:require [cheshire.core :as json])
  )

(defn index-page []
  (html5
   [:head
    [:title "Moodseer Test"]
    (include-css "/css/standard.css")]
   [:body
    [:h1 "Yow! Moodseer Test"]]))


(defn echo-api-command [apicmd]
  (let [cmd (:cmd (first apicmd))
        resp
        (cond
         (= "play" cmd) (moodseer.process/player-play)
         (= "stop" cmd) (moodseer.process/player-stop)
         (= "next" cmd) (moodseer.process/player-next)
         (= "prev" cmd) (moodseer.process/player-prev)
         (= "quit" cmd) nil
         (= "volume" cmd) (moodseer.process/get-player-volume)
         (= "louder" cmd) (moodseer.process/player-volume-up)
         (= "softer" cmd) (moodseer.process/player-volume-down)
         (= "nowplaying" cmd) (moodseer.process/player-nowplaying)
         (= "upcoming"   cmd) (moodseer.process/get-player-upcoming))]
    (cond
      (= "nowplaying" cmd) (cheshire.core/generate-string {:status true :message "Hello" :nowplaying resp})
      (= "upcoming" cmd) (cheshire.core/generate-string {:status true :message "Hello" :upcoming {:songlist resp}})
      (= "volume" cmd) (cheshire.core/generate-string {:status true :volume (:volume resp)})
     true (cheshire.core/generate-string {:testresponse {:message "WTF honey" :muted false :command cmd :params apicmd}}))
    ))
;; (str "<h1>Hey I think it worked: cmd is " cmd "; " apicmd " with " (count apicmd) " params</h2>")))

(deftemplate newville "html/newplayer.html"
  [& payload]
  )
