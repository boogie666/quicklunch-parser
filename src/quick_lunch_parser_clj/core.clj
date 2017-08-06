(ns quick-lunch-parser-clj.core
  (:require [quick-lunch-parser-clj.parser :as p]
            [quick-lunch-parser-clj.utils :as u]
            [clojure.core.cache :as cache]))



(def ql-cache
  (atom (cache/ttl-cache-factory {} :ttl u/half-a-day)))

(defn get-quick-lunch-menu! []
  (let [current-week (u/current-week!)]
    (if (cache/has? @ql-cache current-week)
      (let [updated-cache (swap! ql-cache #(cache/hit % current-week))]
        (get updated-cache current-week))
      (let [fresh-data (-> "http://www.quick-lunch.ro/meniu_curent2.php"
                           (p/get-dom!)
                           (p/parse-quick-lunch-data))
            updated-cache (swap! ql-cache #(cache/miss % current-week fresh-data))]
        (get updated-cache current-week)))))


(get-quick-lunch-menu!)
