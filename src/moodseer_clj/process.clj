(ns moodseer-clj.process
  (:use [clojure.java.shell :only [sh]]))

;; (sh "bash" "-c" "mplayer -playlist /tmp/gort.m3u -shuffle -quiet -slave -input file=/tmp/MYCMD  1>/tmp/mplayer-out.log 2>&1 & \n echo $!")
;; (sh "bash" "-c" "echo 'get_meta_artist\nget_meta_title\nget_meta_album\nget_meta_genre\nget_meta_year\nget_meta_track\nget_meta_comment' > /tmp/MYCMD")
;; (sh "bash" "-c" "echo 'get_percent_pos\nget_time_pos\nget_time_length' > /tmp/MYCMD")
