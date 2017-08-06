(ns quick-lunch-parser-clj.core
  (:require [quick-lunch-parser-clj.parser :as p]
            [quick-lunch-parser-clj.utils :as u]
            [clojure.core.cache :as cache]
            [compojure.route :as route :refer [files not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [ring.util.response :as r]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]))


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



(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Heroku"})

(defroutes handler
  (GET "/menu" []
    (-> (r/response (pr-str (get-quick-lunch-menu!)))
        (r/header "Content-Type" "text/plain"))))

(def ql-api
  (wrap-defaults handler api-defaults))

(defn -main []
  (let [port (env :port)
        join? (env :join?)]
    (run-server #'ql-api {:port (read-string port) :join? join?})))
