(ns org.springframework.security.demo.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [org.springframework.security.demo.handler :refer :all])
  (:import org.eclipse.jetty.servlet.ServletTester
           org.eclipse.jetty.server.Server
           org.eclipse.jetty.server.LocalConnector))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(defn- create-server []
  (let [server (Server.)]
    (doto server 
      (.addConnector (LocalConnector. server)))))

(defn- destroy-server [server]
  (doto server (.stop) (.join)))

(defn- set-configurer [server]
  (doto server (.setHandler (create-handler app))))

(defn- start [server] (doto server (.start)))

(deftest auth-redirect-test 
  (let [server (create-server)
        found? (fn [response text]
                 (is (not (= (. response indexOf text) -1))))]
    (doto (.getResponses
            (-> server (set-configurer) (start) (.getConnectors) (aget 0))
            "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n")
      (found? "302 Found")
      (found? "Location: http://localhost/login"))
    (destroy-server server)))
