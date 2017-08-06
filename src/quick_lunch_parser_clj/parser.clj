(ns quick-lunch-parser-clj.parser
  (:require [quick-lunch-parser-clj.utils :refer [unsplit-words]]
            [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]))


(defn get-dom!
  [url]
  (html/html-snippet
    (:body @(http/get url))))


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


(defn- parse-current-week
  "Returns current quicklunch displayed week"
  [dom]
  (-> dom
    (html/select [:div#saptamana_curenta :div#numar :span])
    first
    :content
    first
    read-string))

(defn- parse-quick-lunch-menu
  "Parses the quicklunch menu html"
  [dom]
  (let [week-titles (get-week-titles dom)
        menu-items  (get-menu-items dom)]
    (map (fn [type dishes] {:type type :dishes dishes})
        week-titles
        (partition 5 menu-items))))


(defn parse-quick-lunch-data [dom]
  (let [menu (parse-quick-lunch-menu dom)
        week (parse-current-week dom)]
    {:week week
     :menu menu}))
