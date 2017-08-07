(ns quick-lunch-parser-clj.core
  (:import (java.io ByteArrayOutputStream))
  (:require [quick-lunch-parser-clj.parser :as p]
            [quick-lunch-parser-clj.utils :as u]
            [clojure.core.cache :as cache]
            [compojure.route :as route :refer [files not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [compojure.core :refer [defroutes GET ANY]]
            [ring.util.response :as r]
            [environ.core :refer [env]]
            [cognitect.transit :as transit]
            [clojure.pprint :as pp]
            [org.httpkit.server :refer [run-server]]))


(defn write-string [x]
  (let [baos (ByteArrayOutputStream.)
        w (transit/writer baos :json)
        _ (transit/write w x)
        out (.toString baos)]
    (.reset baos)
    out))


(defn pritty-print-to-string [x]
  (let [out (java.io.StringWriter.)]
    (pp/pprint x out)
    (.toString out)))

(def ql-cache
  (atom (cache/ttl-cache-factory {} :ttl u/half-a-day)))

(defn get-quick-lunch-menu! []
  (let [current-week (u/current-week!)]
    (if (cache/has? @ql-cache current-week)
      (let [updated-cache (swap! ql-cache #(cache/hit % current-week))]
        (get updated-cache current-week))
      (let [fresh-data (-> (env :ql-endpoint)
                           (p/get-dom!)
                           (p/parse-quick-lunch-data))
            updated-cache (swap! ql-cache #(cache/miss % current-week fresh-data))]
        (get updated-cache current-week)))))


(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Heroku"})

(defn formated-response [formatter]
  (-> (r/response (formatter (get-quick-lunch-menu!)))
      (r/header "Content-Type" "text/plain")))

(defroutes handler
  (GET "/transit" []
    (formated-response write-string))

  (GET "/clj" []
    (formated-response pr-str))

  (GET "/clj-pritty" []
    (formated-response pritty-print-to-string))

  (ANY "*" []
    (r/not-found "Not found!")))

(comment
  (println
    (:body (handler {:protocol       "HTTP/1.1"
                     :server-port    8080
                     :server-name    "blah"
                     :remote-addr    "localhost"
                     :uri            "/clj-pritty"
                     :request-method :get
                     :headers        {"host" "localhost:8080"}}))))

(def ql-api
  (wrap-defaults handler api-defaults))

(defn -main []
  (let [port (env :port)
        join? (env :join?)]
    (run-server #'ql-api {:port (read-string port) :join? (read-string join?)})))
