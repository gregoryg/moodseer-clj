(ns moodseer.process
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.string :as str]))


(defn mpc-key->keyword [s]
  (->>
   (str/replace s #"(ANS_META_|ANS_)" "")
   str/lower-case
   keyword
   ))

(defn mpc-pairs->map [& [pairs]]
  (let [pairs (str/replace pairs "=\n" "=nil")] ;; egregious work-around for last value null
  (->>
   (clojure.string/split pairs #"[=\n]")
   (map str/trim)
   (map-indexed (fn [i x] (if (odd? i) x (mpc-key->keyword x))))
   (apply hash-map)
   )))

(defn player-play []
  (sh "mpc" "play"))

(defn player-pause []
  (sh "mpc" "toggle"))

(defn player-stop []
  (sh "mpc" "stop"))


(defn player-next []
  (sh "mpc" "next"))

(defn player-prev []
  (sh "mpc" "prev"))

(defn player-set-volume [n]
  "Set volume by integer 0-100, or increment (with + or - prefix)"
  (sh "mpc" "volume" (str n)))

(defn player-volume-up 
  ([] (player-volume-up 5))
  ([n]
     (player-set-volume (str "+" n))))

(defn player-volume-down
  ([] (player-volume-down 5))
  ([n]
     (player-set-volume (str "-" n))))
    
(defn player-nowplaying []
  (->> 
   (sh "mpc" "current" "--format" "file=%file%\nartist=%artist%\nalbum=%album%\nsong=%title%\ntrack-num=%track%\ntime=%time%\n")
   :out
   mpc-pairs->map
   ))

(defn get-player-progress []
  (let [x (:out (sh "mpc" "--format" "file=%file%\ntime=%time%\n"))
        ; pick out components: playlist_pos playlist_count time_played time_length percent_played
        progress-fields [:state :playlist-pos :playlist-count :time-played :song-length :percent-played]
        y  (into {} (map #(hash-map %1 %2)
                         progress-fields
                         (rest (first (re-seq #"\n?\[(playing|paused)\]\s+#([0-9]+)/([0-9]+)\s+([0-9:]+)/([0-9:]+)\s+\(([0-9]+)%\)" x)))
                         ))
        z (into y (map #(hash-map (keyword (nth % 1)) (nth % 2))
                       (re-seq #"(volume|repeat|random|single|consume):\s+([^ \n]+)" x)))
        zz (conj
            (into z (map #(hash-map (keyword (nth % 1)) (nth % 2))
                         (re-seq #"(file|time)=([^\n]+)" x)))
            {:paused (= (:state z) "paused")})
        ]
    zz))

(defn get-player-volume []
  (first
   (map #(hash-map (keyword (nth % 1)) (nth % 2))
        (re-seq #"(volume):\s*([0-9]+)" (:out (sh "mpc" "volume"))))))

(defn get-player-upcoming
  "Get pre-formatted upcoming songs"
  ([]
     (get-player-upcoming 10))
  ([n]
     (let [pos (+ n (Integer/parseInt (:playlist-pos (get-player-progress))))
           x (:out (sh "bash" "-c" (str "mpc playlist | head -" pos " | tail -" n)))]
         (str/split x #"\n"))))

(defn get-player-upcoming-paths
  "Get paths to files in upcoming list"
  ([] (get-player-upcoming-paths 10))
  ([n]
     (let [pos (+ n (Integer/parseInt (:playlist-pos (get-player-progress))))
           x (:out (sh "bash" "-c" (str "mpc listall | head -" pos " | tail -" n)))]
         x)))
  
