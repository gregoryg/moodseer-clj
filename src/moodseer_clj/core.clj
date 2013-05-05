(ns moodseer-clj.core
  (:use 
   [compojure.core]
   [compojure.route :as route]
   [hiccup.page]
   [hiccup.form]
   [hiccup.def]
   [net.cgrand.enlive-html :as html :only [deftemplate at content set-attr attr? strict-mode defsnippet]])
;;  (:require [noir.server :as server])
  (:require [moodseer-clj.ajax])
  (:require [cheshire.core :as json])
  (:import (java.io StringReader BufferedReader)))

(def starting-links [{:desc "Escape to the mothership" :url "http://www.google.com" :important true}
                     {:desc "First test page GORT" :url "/gort"}
                     {:desc "Moodseer HTML5 Clojure" :url "/zoneplayer" :important true}])

(defn gort-message []
  (html5
   [:body
    (form-to [:post "/"]
             (text-area {:placeholder "say something..."} "message")
             [:br]
             (text-field {:placeholder "name"} "id")
             (submit-button "post message"))]))

(defn display-gort-message [params]
  (let [form-params (:form-params params)]
    (html5
     [:body
      [:p (get form-params "id") " says " (get form-params "message")]])))

(defroutes app-routes
  (GET "/" [] (gort-message))
  (POST "/" params (display-gort-message params))
  (route/resources "/")
  (route/not-found "Not found"))


;; (defpage "/" []
;;   (html5 [:h1 "Hello World"]
;;          (include-css "/css/standard.css")
;;          [:h2 "First sub-section header"]
;;          [:ul
;;           (for [link starting-links]
;;           [:li [:a {:href (:url link)} (:desc link)]])
;;           ]))

(deftemplate index "html/gort.html"
  [ctxt]
  [:div#preamble] (html/content (:message ctxt))
  )

(hiccup.def/defhtml layout [& content]
  (html5
   [:head
    [:title "My Moodseer Pang"]
    (include-css "/css/standard.css")]
   [:body
    [:div#wrapper
    content]]))

(hiccup.def/defhtml api [params & [content]]
  (let [params (into {} params)
        cmd (:cmd params)
        resp
        (cond
          (= "play" cmd) (moodseer-clj.process/player-play)
          (= "pause" cmd) (moodseer-clj.process/player-pause)
          (= "stop" cmd) (moodseer-clj.process/player-stop)
          (= "next" cmd) (moodseer-clj.process/player-next)
          (= "prev" cmd) (moodseer-clj.process/player-prev)
          (= "quit" cmd) nil
          (= "volume" cmd) (moodseer-clj.process/get-player-volume)
          (= "louder" cmd) (moodseer-clj.process/player-volume-up)
          (= "softer" cmd) (moodseer-clj.process/player-volume-down)
          (= "nowplaying" cmd) (moodseer-clj.process/player-nowplaying)
          (= "upcoming"   cmd) (moodseer-clj.process/get-player-upcoming))
        ]
    (cond
      (= "nowplaying" cmd) (cheshire.core/generate-string {:status true :message "Hello" :nowplaying resp})
      (= "upcoming" cmd) (cheshire.core/generate-string {:status true :message "Hello" :upcoming {:songlist resp}})
      (= "volume" cmd) (cheshire.core/generate-string {:status true :volume (:volume resp)})
      true      (cheshire.core/generate-string {:testresponse {:message "WTF honey" :muted false :command  cmd}}))))

;; (defpage [:get "/api"] params
;;   (api params))

;; (defpage "/api:cmd" {:as params}
;;   (api params))

(deftemplate zone-player "html/tsplayer.html"
  [& payload]
  )

;; (deftemplate footer "html/footer.html" [& payload])

;; (defpage "/zoneplayer" []
;;   (zone-player))

;; (defpage "/gort" []
;;   (layout 
;;    (index {:message "That's the stuff"})
;;    [:p "Moodseer test..."]
;;    )
;;   )


(deftemplate newville "html/newplayer.html" 
  [& payload]
  )

;; (defpage "/newville" []
;;   (newville))

;; (defpage "/api/upcoming" []
;;   (json/generate-string upcoming-test-list))

;; (defn -main [& [port]]
;;   (server/start (Integer. (or port "8080"))))

;;(defonce moodseer-server (server/start 8080))
