(ns quick-lunch-parser-clj.utils
  (:require [clojure.string :as str]))


(defn week-of-year
  "Returns week of year for given date"
  [date]
  (let [cal (java.util.Calendar/getInstance)]
    (doto cal
      (.setTime date)
      (.set java.util.Calendar/HOUR 0)
      (.set java.util.Calendar/MINUTE 0)
      (.set java.util.Calendar/SECOND 0)
      (.set java.util.Calendar/MILLISECOND 0))
    (.get cal java.util.Calendar/WEEK_OF_YEAR)))

(defn unsplit-words
  "Unsplits hyphonated words.
   Ex: e-
       le-
       phant
    becomes 'elephant'"
  [words]
  (loop  [word (first words)
          words (rest words)]
    (if (seq words)
      (if (str/ends-with? word "-")
        (recur (str (str/join "" (butlast word)) (first words)) (rest words))
        (recur (str word " " (first words)) (rest words)))
      word)))

(defn current-week! []
  (week-of-year (new java.util.Date)))

(def half-a-day
  (* 1000 ;milliseconds -> seconds
     60   ;seconds -> minutes
     60   ;mins -> hours
     12))
