(ns moodseer-clj.ajax
  (:use [net.cgrand.enlive-html :as html])
  (:use [moodseer-clj.db])
  (:use [cheshire.core :as json]))

(def upcoming-test-list
  {:request-type "upcoming",
					 :status 0,
					 :requests [
					 {:song "Sweet Memory" :artist "Melody Gardot" :is-request true}
					 {:song "Rhumba Girl" :artist "Nicolette Larson"}
					 {:song "Church" :artist "Lyle Lovett"}]})

(defn upcoming
  "Return list of upcoming songs on the Moodseer server for the given station/stream"
  []
  (json/generate-string upcoming-test-list))

