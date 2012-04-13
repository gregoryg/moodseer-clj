(ns moodseer-clj.db
  (:refer-clojure :exclude [replace])
  (:require [clojure.java.jdbc :as sql]))

(def db {:classname "com.mysql.jdbc.Driver"
	 :subprotocol "mysql"
	 :user "gregj"
	 :password "dbuser"
	 :subname "//localhost:3306/moodseer"})





(def *gort* (sql/with-connection db
	      (sql/with-query-results res 
		["SELECT id,title,genre FROM song WHERE genre IN('General Latin','Latin','Salsa','Brazilian','French') ORDER BY RAND() LIMIT 10"]
		(doall res))))

(def artist-default-fields [:artist.id :artist])
(def song-default-fields [:song.id :song.title :filename :track_num])
(def album-default-fields [:disc.id :disc.title :coverart_url])

(defn keys-to-query-string
  "Transform [:id :title] to \"id, title\""
  [vector-of-keys]
  (clojure.string/join ", " (map name vector-of-keys)))

(defn db-query
  "Return results from a query to the default database"
  [sql-query]
  (sql/with-connection db
    (sql/with-query-results res
      [(str sql-query)]
      (doall res))))

(defn get-album [albumid & [include-tracks?]]
  (let [album-info  (db-query (str "SELECT " (keys-to-query-string album-default-fields) ",artist.artist AS 'album-artist'
                                  FROM disc,artist WHERE (disc.artistid = artist.id) AND disc.id = '" albumid "'"))
        track-info (when include-tracks? (db-query
                             (str "SELECT " (keys-to-query-string song-default-fields) ",artist.artist AS 'track-artist'
                            FROM song LEFT JOIN artist ON (song.artistid = artist.id) WHERE song.discid = '" albumid "'"
                            " ORDER BY track_num, song.title")))

        ]
    {:album-info album-info :track-info track-info}
    ))


;; (defn get-album-first-chars
;;  "Return first letters of album titles in DB"
;;  [& [{:keys [regexp has_coverart?] :or {regexp "^." has_coverart? true}}]]
;;  (println (format "regexp: %s, has_coverart?: %s" regexp has_coverart?))
;; )
;;  ;; (db-query "SELECT SUBSTRING(disc.title,1,1) AS firstletter, COUNT(*) AS cnt
;;  ;;                FROM disc WHERE disc.title REGEXP


(defn get-album-first-chars
  ([] (get-album-first-chars "^." false))
  ([regexp] (get-album-first-chars regexp false))
  ([regexp has-coverart?]
  (println (format "regexp: %s, has-coverart?: %s" regexp has-coverart?))))



(defn get-artist-metadata-for-browse
  ([] (get-artist-metadata-for-browse 4 :artist 0, "a", nil))
  ([window] (get-artist-metadata-for-browse window :artist 0, "a", nil))
  ([window sort-by] (get-artist-metadata-for-browse window sort-by, 0, "a", nil))
  ([window sort-by offset] (get-artist-metadata-for-browse window sort-by offset "a" nil))
  ([window sort-by offset first-letter] (get-artist-metadata-for-browse window sort-by offset first-letter nil))
  ([window sort-by offset first-letter last-letter]
     (let [offset-next (if (< offset 0) (str "0," (. Math max 0 (- window (. Math abs offset)))) (str offset "," window))
           offset-prev (str (if (>= offset 0) "0,0" (str (. Math abs (. Math min 0 (- window (. Math abs offset)))) "," (. Math min window (. Math abs offset)))))
           q (str "(SELECT " (keys-to-query-string artist-default-fields)
                  ", COUNT(disc.id) AS num_discs "
                    "FROM disc,artist WHERE disc.artistid=artist.id "
                    "AND artist.artist >= '" first-letter "'"
                    (when last-letter (str " AND artist.artist <= '" last-letter "'"))
                    " GROUP BY artist.artist"
                    " ORDER BY " (name sort-by)
                    " LIMIT " offset-next ")")]
       (db-query q)
  )))

