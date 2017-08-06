(ns quick-lunch-parser-clj.core
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as str]))

(def menu-text (ref (slurp "resources/meniu_curent2.php")))

(defn get-dom
  [url]
  (html/html-snippet
    (:body @(http/get url))))

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


(defn- parse-day-item [index day-item-dom]
  {:title (-> day-item-dom (html/select [:span]) first :content first)
   :price (-> day-item-dom (html/select [:span :b]) first :content first read-string)
   :day (+ 1 (mod index 5))})

(defn- parse-week-title [item]
  {:title (-> item (html/select [:font]) first :content first)
   :sub-title (unsplit-words
                (filter string?
                  (-> item
                    (html/select [:span])
                    first
                    :content)))})

(defn- get-menu-items [dom]
  (let [items (html/select dom [[:table (html/attr= :width "142")]])]
    (map-indexed parse-day-item items)))

(defn- get-week-titles [dom]
  (let [week-items (html/select dom [[:td (html/attr= :width "50")]])]
    (into [] (comp (map parse-week-title) (filter :title)) week-items)))


(defn get-current-week
  "Returns current quicklunch displayed week"
  [dom]
  (-> dom
    (html/select [:div#saptamana_curenta :div#numar :span])
    first
    :content
    first
    read-string))

(defn parse-quick-lunch-menu
  "Parses the quicklunch menu html"
  [dom]
  (let [week-titles (get-week-titles dom)
        menu-items  (get-menu-items dom)]
    (map (fn [type dishes] {:type type :dishes dishes})
        week-titles
        (partition 5 menu-items))))


(defn week-of-year
  "Returns week of year for given date"
  [date]
  (let [cal (java.util.Calendar/getInstance)]
    (doto cal
      (.setFirstDayOfWeek java.util.Calendar/MONDAY)
      (.setTime date)
      (.set java.util.Calendar/HOUR 0)
      (.set java.util.Calendar/MINUTE 0)
      (.set java.util.Calendar/SECOND 0)
      (.set java.util.Calendar/MILLISECOND 0))
    (.get cal java.util.Calendar/WEEK_OF_YEAR)))


(comment
  (def ql-html (get-dom "http://www.quick-lunch.ro/meniu_curent2.php"))

  (do ql-html)
  (clojure.pprint/pprint
    (parse-quick-lunch-menu (get-dom "http://www.quick-lunch.ro/meniu_curent2.php"))))
