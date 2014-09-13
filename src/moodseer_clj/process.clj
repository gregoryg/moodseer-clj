(ns moodseer.process
  (:use [clojure.java.shell :only [sh]])
  (:use [clojure.string :only [split join]])
  (:require [clojure.string :as str]))
(def zones [{:id 2 :desc "Old laptop"  :host "172.16.17.101"   :port 6600 :moodseer-key nil},
            {:id 0 :desc "Living Room" :host "molly.magichome" :port 6600 :moodseer-key "/home/gregj/.ssh/moodseer-molly_dsa"},
            {:id 1 :desc "Master Bedroom" :host "moodspot1.magichome" :port 6700 :moodseer-key nil}])


(defn mpc-base-cmd  []
  (let [zoneparams (nth zones 0 nil)]
 [ "mpc" "--port" (str (:port zoneparams)) "--host" (:host zoneparams) ]
 )
)

(defn mpc-key->keyword [s]
  (->>
   (str/replace s #"(ANS_META_|ANS_)" "")
   str/lower-case
   keyword
   ))

(defn mpc-pairs->map [& [pairs]]
  (let [pairs (str/replace
               (str/replace pairs "=\r?\n" "=nil")  ;; egregious work-around for last value null
               "\r" "")] 
    (if (= "" pairs)
      nil
      (->>
       (split pairs #"[=\n]")
       (map str/trim)
       (map-indexed (fn [i x] (if (odd? i) x (mpc-key->keyword x))))
       (apply hash-map)))))

(defn player-play []
  (apply sh (into (mpc-base-cmd) ["play"])))

(defn player-pause []
  (apply sh (into (mpc-base-cmd) ["toggle"])))

(defn player-stop []
  (apply sh (into (mpc-base-cmd) ["stop"])))


(defn player-next []
  (apply sh (into (mpc-base-cmd) ["next"])))

(defn player-prev []
  (apply sh (into (mpc-base-cmd) ["prev"])))

(defn player-set-volume [n]
  "Set volume by integer 0-100, or increment (with + or - prefix)"
  (apply sh (into (mpc-base-cmd) ["volume" (str n)])))

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
   (apply sh (into (mpc-base-cmd)  ["current" "--format" "file=%file%\nartist=%artist%\nalbum=%album%\nsong=%title%\ntrack-num=%track%\ntime=%time%\n"]))
   :out
   mpc-pairs->map
   ))

(defn get-player-progress []
  (let [x (:out (sh "bash" "-c" (str (join " "  (mpc-base-cmd)) " --format file=%file%\\\\ntime=%time%\\\\n")))
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
        (re-seq #"(volume):\s*([0-9]+)" (:out (apply sh (into (mpc-base-cmd) ["volume"])))))))

(defn get-player-upcoming
  "Get pre-formatted upcoming songs"
  ([]
     (get-player-upcoming 10))
  ([n]
     (let [pos (+ n (Integer/parseInt (:playlist-pos (get-player-progress))))
           x (:out (sh "bash" "-c" (str (join " " (mpc-base-cmd)) " playlist | head -" pos " | tail -" n)))]
         (str/split x #"\n"))))

(defn get-player-upcoming-paths
  "Get paths to files in upcoming list"
  ([] (get-player-upcoming-paths 10))
  ([n]
     (let [pos (+ n (Integer/parseInt (:playlist-pos (get-player-progress))))
           x (:out (sh "bash" "-c" (str (join " " (mpc-base-cmd)) " listall | head -" n)))]
         (split x #"\n"))))

(defn player-say [sayphrase]
  (sh "ssh" "-i" ((nth zones 0 nil) :moodseer-key) ((nth zones 0 nil) :host) "espeak '" sayphrase "'"))
  ;; (sh "ssh" "-i" "/home/gregj/.ssh/moodseer-molly_dsa" "molly.magichome" "espeak '" sayphrase "'"))
