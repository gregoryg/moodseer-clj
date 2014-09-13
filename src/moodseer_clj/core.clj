(ns moodseer-clj.core
  (:use compojure.core
        ring.middleware.json
        ring.middleware.file
        ;; [ring.middleware.multipart-params]
        selmer.parser
        )
  (:require [compojure.route :as route]
            ;; [compojure.handler :as handler] ;; depends on ring.middleware.multipart-params, which I can't find
            [compojure.response :as response]
            [ring.middleware.keyword-params :refer :all]
            [ring.middleware.params :refer :all]
            [ring.middleware.nested-params :refer :all]
            ;; [ring.middleware.multipart-params] 
            ;; [selmer.parser :refer [render-file]]
            [org.httpkit.server :refer [run-server]])) ; httpkit is a server

;; (defroutes myapp
;;   (GET "/" [] "Hello World"))
(defroutes app
  (GET "/gort" req {:this "this" :that "other"})
  (GET "/webpage" req (selmer.parser/render-file "newplayer.html" {}))
  (GET "/" req "Do something with req")
  (GET "/player" req (render-file "player.html" {:moodseer-title "Moodseer New Horizons" :moodseer-announce "THIS IS MY H1"}))
  (GET ["/api/:cmd"] [cmd]
       (ring.util.response/response
       (let [resp 
             (cond (= "play" cmd)
                   (moodseer.process/player-play)
                   (= "stop" cmd)
                   (moodseer.process/player-stop)
                   (= "pause" cmd)
                   (moodseer.process/player-pause)
                   (= "upcoming" cmd)
                   (moodseer.process/get-player-upcoming)
                   )]
         {:testresponse {:message "WTF honey" :muted false :command cmd :response resp}})))
         (GET ["/file/:name.:ext" :name #".*", :ext #".*"] [name ext]
              (str "File: " name ", extention " ext))
         (GET "/posts" {params :params}
              (str "Title is " (:title params) ", author is " (:author params))
              )
         ;; (str "Do something with " title " and " author)))
         (GET "/math" req
              ;; (get params :a) ;; 1
              ;; (str "a=" (:a (:params req)) ", b=" (:b (:params req)))
              (pr-str req)
              )
         (GET "/play/:track" [track]
              (str "Play API: track is "track)
              )

         (POST "/" [] "Create something")
         (PUT "/" [] "Replace something")
         (PATCH "/" [] "Modify Something")
         (DELETE "/" [] "Annihilate something")
         (OPTIONS "/" [] "Appease something")
         (HEAD "/" [] "Preview something")
         (route/not-found "Not Found")
         )

       ;; (def handler 
       ;;   (-> app
       ;;       (handler/api) ;; several middleware wrapping functions, including the one to bind params
       ;;       ))

       ;; custom middleware
       (defn allow-cross-origin
  "middleware to allow cross origin"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Access-Control-Allow-Origin"]
                "*"))))

       (defn set-json-content
  "middleware to set media type as application/json"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"]
                "application/json; charset=utf-8"))))

       ;; hendler
       (def handler
         (-> app
             (wrap-file "resources")
             ;; wrap-keyword-params
             ;; wrap-nested-params
             wrap-params
             wrap-json-body
             wrap-json-response
             ))


       (defonce server (atom nil))

       (defn stop-server []
         (when-not (nil? @server)
           ;; graceful shutdown: wait 100ms
           ;; timeout is optional; when no timeout, stop immediately
           (@server :timeout 100)
           (reset! server nil)))

       (defn -main []
         ;; The #' is useful when you want to hot-reload code
         ;; You may want to take a look: https://github.com/clojure/tools.namespace
         ;; and http://http-kit.org/migration.html#reload
         ;; (reset! server (run-server #'app {:port 5000})))
         (reset! server (run-server #'handler {:port 5000})))
       ;; (run-server myapp {:port 5000}))
