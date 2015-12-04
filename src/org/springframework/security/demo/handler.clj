(ns org.springframework.security.demo.handler
  (:require [clojure.string :refer [split]] 
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.servlet :refer 
             [servlet build-request-map update-servlet-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.logger.protocols :refer [Logger]]
            [taoensso.timbre :refer [merge-config!] :as timbre]
            [taoensso.timbre.appenders.core :refer [spit-appender]])
  (:import [org.springframework.web.context.support
             WebApplicationContextUtils AnnotationConfigWebApplicationContext]
           org.springframework.security.demo.SecurityAnnotationConfiguration
           [org.eclipse.jetty.webapp WebAppContext Configuration]
           [org.eclipse.jetty.servlet ServletContextHandler ServletHolder]))

;setting 

(defonce servername "jetty-spring")
(defonce portnumber 8080)
(defonce hostname "localhost") 
(defonce logfile-path "file.log")

;application

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

;logger-middleware-impl

(defn- genkey-from-appender [appender]
  (keyword (first (split (str appender) #"-"))))

(defn- make-appender-options [appender & [options]]
  {(genkey-from-appender appender)
     (if options
       (appender options)
       (appender))})

(defn- make-timbrelogger [appender & [options]]
  (do (merge-config! 
        {:appenders
           (if options 
             (make-appender-options appender options)
             (make-appender-options appender))})
      (reify Logger
        (add-extra-middleware [_ handler] handler)
        (log [_ level throwable message]
          (timbre/log level throwable message)))))

(defmacro defspitloggergenerator [name# fpath#]
  `(defn ~name# []
     (make-timbrelogger spit-appender 
                        {:fname ~fpath#})))          

(defspitloggergenerator gen-spitter logfile-path)
(def spitter (gen-spitter)) 

;appserver-security

(defn- create-handler [app]
  (doto (WebAppContext.)
    (.setConfigurations
      (into-array Configuration [(SecurityAnnotationConfiguration.)]))
    (.addServlet 
      (doto (ServletHolder. (servlet app)) (.setName "default")) 
      "/")))

(defn- build-spring-cfg [app]
  (fn [server]
    (doto server
      (.setHandler (create-handler app)))))

(defn- run-jetty-wrapper 
  [handler cfg-builder options]
  (run-jetty (fn [] nil) ;dummy function
             (assoc options :configurator 
                            (cfg-builder handler)))) 

;driver 

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      (wrap-with-logger {:logger spitter}))) 

(defn -main []
  (run-jetty-wrapper app 
    build-spring-cfg {:servname servername 
                      :port portnumber 
                      :host hostname}))
