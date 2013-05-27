(ns moodseer.routes
  (:use compojure.core
        moodseer.views
        moodseer.process
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/" [] (index-page))
  (GET "/newville" [] (newville))
  (GET "/api" [ & more ] (echo-api-command [more]))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))

