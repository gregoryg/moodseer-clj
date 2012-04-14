(ns moodseer-clj.process
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.string :as str]))

;; (sh "bash" "-c" "mplayer -playlist /tmp/gort.m3u -shuffle -quiet -slave -input file=/tmp/MYCMD  1>/tmp/mplayer-out.log 2>&1 & \n echo $!")
;; (sh "bash" "-c" "echo 'get_meta_artist\nget_meta_title\nget_meta_album\nget_meta_genre\nget_meta_year\nget_meta_track\nget_meta_comment' > /tmp/MYCMD")
;; (sh "bash" "-c" "echo 'get_percent_pos\nget_time_pos\nget_time_length' > /tmp/MYCMD")

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


(defn start-player [& playlist]
  (do
    (mkfifo)
    (Thread/sleep 200)
    (println (str player-cmd " " (clojure.string/join " " playlist) " 1>/tmp/mplayer-out.log 2>&1 & \n echo $!"))
    (sh "bash" "-c" (str player-cmd " " (first playlist) " 1>/tmp/mplayer-out.log 2>&1 & \n echo $!"))))

(defn pause-player []
  (sh "bash" "-c" (str "echo 'pause' > " fifo-path) ))

(defn step-player
  ([] (step-player 1))
  ([step]
     (sh "bash" "-c" (str "echo 'pt_step " step "' > " fifo-path))))

(defn next-player []
  (step-player 1))

(defn prev-player []
  (step-player -1))

(defn quit-player []
  (sh "bash" "-c" (str "echo 'quit' > " fifo-path) ))

(defn get-player-track-info []
  (let [x (sh "bash" "-c" "echo 'get_meta_artist\nget_meta_title\nget_meta_album\nget_meta_genre\nget_meta_year\nget_meta_track\nget_meta_comment' > /tmp/MYCMD")
        _ (Thread/sleep 200)
        y (:out (sh "bash" "-c" "tail -7 /tmp/mplayer-out.log | egrep '^ANS_META' | sed \"s,',,g\""))
        ]
    y))

(defn get-player-progress []
  (let [x (sh "bash" "-c" "echo 'get_percent_pos\nget_time_pos\nget_time_length' > /tmp/MYCMD")
        _ (Thread/sleep 200)
        y (:out (sh "bash" "-c" "tail -3 /tmp/mplayer-out.log | egrep '^ANS_' | sed \"s,',,g\""))
        ]
    y))
