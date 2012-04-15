(ns moodseer-clj.process
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.string :as str]))

;; (sh "bash" "-c" "mplayer -playlist /tmp/gort.m3u -shuffle -quiet -slave -input file=/tmp/MYCMD  1>/tmp/mplayer-out.log 2>&1 & \n echo $!")
;; (sh "bash" "-c" "echo 'get_meta_artist\nget_meta_title\nget_meta_album\nget_meta_genre\nget_meta_year\nget_meta_track\nget_meta_comment' > /tmp/MYCMD")
;; (sh "bash" "-c" "echo 'get_percent_pos\nget_time_pos\nget_time_length' > /tmp/MYCMD")
;; mpc current --format "file=%file%\nartist=%artist%\nalbum=%album%\nsong=%title%\ntrack-num=%track%\ntime=%time%\n"

;; file=Various-Brazil_The_Music_of/02.Dominguinhos_Convidados_(w_Marciel_Melo)-A_Volta_Da_Asa_Branca.mp3
;; artist=Dominguinhos & Convidados (w/ Marciel Melo)
;; album=Brazil, The Music of
;; song=A Volta Da Asa Branca
;; track-num=02
;; time=3:49
;; ORBIT_SOCKETDIR=/tmp/orbit-gregj


(def fifo-path "/tmp/MYCMD")

(def player-cmd (str "/usr/bin/mplayer -quiet -slave -input file=" fifo-path " -idle -nocache -nortc"))


(defn mplayer-key->keyword [s]
  (->>
   (str/replace s #"(ANS_META_|ANS_)" "")
   str/lower-case
   keyword
   ))

(defn mplayer-pairs->map [& [pairs]]
  (let [pairs (str/replace pairs "=\n" "=nil")] ;; egregious work-around for last value null
  (->>
   (clojure.string/split pairs #"[=\n]")
   (map str/trim)
   (map-indexed (fn [i x] (if (odd? i) x (mplayer-key->keyword x))))
   (apply hash-map)
   )))

(defn mkfifo []
  (let [fifo-path "/tmp/MYCMD"]
    (do
      (sh "rm" "-f" fifo-path)
      (sh "mkfifo" fifo-path))))


;; (defn start-player [& playlist]
;;   (do
;;     (mkfifo)
;;     (Thread/sleep 200)
;;     (println (str player-cmd " " (clojure.string/join " " playlist) " 1>/tmp/mplayer-out.log 2>&1 & \n echo $!"))
;;     (sh "bash" "-c" (str player-cmd " " (first playlist) " 1>/tmp/mplayer-out.log 2>&1 & \n echo $!"))))

;; (defn pause-player []
;;   (sh "bash" "-c" (str "echo 'pause' > " fifo-path) ))
(defn play-player []
  (sh "mpc" "play"))

(defn pause-player []
  (sh "mpc" "toggle"))



;; (defn step-player
;;   ([] (step-player 1))
;;   ([step]
;;      (sh "bash" "-c" (str "echo 'pt_step " step "' > " fifo-path))))

(defn next-player []
  (sh "mpc" "next"))

(defn prev-player []
  (sh "mpc" "prev"))

(defn quit-player []
  (sh "bash" "-c" (str "echo 'quit' > " fifo-path) ))

(defn get-player-track-info []
  (let [x (sh "bash" "-c" "echo 'get_meta_artist\nget_meta_title\nget_meta_album\nget_meta_genre\nget_meta_year\nget_meta_track\nget_meta_comment' > /tmp/MYCMD")
        _ (Thread/sleep 20)
        y (:out (sh "bash" "-c" "tail -7 /tmp/mplayer-out.log | egrep '^ANS_META' | sed \"s,',,g\""))
        ]
    y))

(defn MPLAYER-get-player-progress []
  (let [x (sh "bash" "-c" "echo 'get_percent_pos\nget_time_pos\nget_time_length' > /tmp/MYCMD")
        _ (Thread/sleep 200)
        y (:out (sh "bash" "-c" "tail -3 /tmp/mplayer-out.log | egrep '^ANS_' | sed \"s,',,g\""))
        ]
    y))

(defn player-nowplaying []
  (->> 
   (sh "mpc" "current" "--format" "file=%file%\nartist=%artist%\nalbum=%album%\nsong=%title%\ntrack-num=%track%\ntime=%time%\n")
   :out
   mplayer-pairs->map
   ))

(defn get-player-progress []
  (let [x (:out (sh "mpc" "--format" "file=%file%\ntime=%time%\n"))
        ;; _ (println x)
        ; pick out components: playlist_pos playlist_count time_played time_length percent_played
        progress-fields [:state :playlist-pos :playlist-count :time-played :song-length :percent-played]
        y  (into {} (map #(hash-map %1 %2)
                         progress-fields
                         (rest (first (re-seq #"\n?\[(playing|paused)\]\s+#([0-9]+)/([0-9]+)\s+([0-9:]+)/([0-9:]+)\s+\(([0-9]+)%\)" x)))
                         ))
        z (into y (map #(hash-map (keyword (nth % 1)) (nth % 2))
                       (re-seq #"(volume|repeat|random|single|consume):\s+([^ \n]+)" x)))
        ]
    z))

(defn get-player-upcoming
  "Get pre-formatted upcoming songs"
  ([] (get-player-upcoming 10))
  ([n] (let [x (:out (sh "bash" "-c" (str "mpc playlist | head -" n)))]
         (str/split x #"\n"))))

(defn get-player-upcoming-paths
  "Get paths to files in upcoming list"
  ([] (get-player-upcoming-paths 10))
  ([n] (let [x (:out (sh "bash" "-c" (str "mpc listall | head -" n)))]
         x)))
  