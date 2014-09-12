(ns moodseer-clj.core
  (:use compojure.core
        ;; [ring.middleware.json :only (wrap-json-response)]
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
  (GET "/" req "Do something with req")
  (GET "/player" req (render-file "player.html" {:moodseer-title "Moodseer New Horizons" :moodseer-announce "THIS IS MY H1"}))
  (GET ["/file/:name.:ext" :name #".*", :ext #".*"] [name ext]
       (str "File: " name ", extenstion " ext))
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
  (HEAD "/" [] "Preview something"))

;; (def handler 
;;   (-> app
;;       (handler/api) ;; several middleware wrapping functions, including the one to bind params
;;       ))

(def handler
  (-> app
      wrap-keyword-params
      wrap-nested-params
      wrap-params))


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
