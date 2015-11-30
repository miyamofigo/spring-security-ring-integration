(ns org.springframework.security.demo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.servlet :refer 
             [servlet build-request-map update-servlet-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import [org.springframework.web.context.support
             WebApplicationContextUtils AnnotationConfigWebApplicationContext]
           org.springframework.security.demo.SecurityAnnotationConfiguration
           [org.eclipse.jetty.server 
             NetworkTrafficServerConnector Handler Connector]
           org.eclipse.jetty.server.handler.HandlerCollection
           [org.eclipse.jetty.webapp WebAppContext Configuration]
           [org.eclipse.jetty.servlet ServletContextHandler ServletHolder]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn create-handlers [app]
  (doto (HandlerCollection.)
    (.setHandlers 
      (into-array Handler
        [(doto (WebAppContext.)
           (.setConfigurations
              (into-array Configuration [(SecurityAnnotationConfiguration.)]))
           (.addServlet (doto (ServletHolder. (servlet app))
                          (.setName "default"))
                        "/"))]))))

(defn build-spring-cfg [app]
  (fn [server]
    (doto server
      (.setHandler (create-handlers app)))))

(defn run-jetty-wrapper 
  [handler cfg-builder options]
  (run-jetty (fn [] nil) ;dummy function
             (assoc options :configurator 
                            (cfg-builder handler)))) 

(defn -main []
  (run-jetty-wrapper app 
    build-spring-cfg {:servname "jetty-spring"
                      :port 8080 
                      :host "localhost"}))
