(ns org.springframework.security.demo.handler
  (:gen-class)
  (:require [clojure.string :refer [split]] 
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.servlet :refer 
             [servlet build-request-map update-servlet-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.logger.protocols :refer [Logger]]
            [taoensso.timbre :refer [merge-config! swap-config!] :as timbre]
            [taoensso.timbre.appenders.core :refer [spit-appender]])
  (:import [org.springframework.web.context.support
             WebApplicationContextUtils AnnotationConfigWebApplicationContext]
           org.eclipse.jetty.annotations.AnnotationConfiguration
           org.eclipse.jetty.util.resource.Resource
           [org.eclipse.jetty.webapp WebAppContext Configuration]
           [org.eclipse.jetty.servlet ServletContextHandler ServletHolder]))

;setting 

(defonce servername "jetty-spring")
(defonce portnumber 8080)
(defonce hostname "localhost") 
(defonce logfile-path "file.log")
(defonce pkg-name "org.springframework.security.demo")

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
  (do (swap-config! 
        #(assoc-in % [:appenders :println :async?] true)) 
      (merge-config! 
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
                        {:fname ~fpath#
                         :async? true})))          

(defspitloggergenerator gen-spitter logfile-path)
(def spitter (gen-spitter)) 

;appserver-security

(defn- add-container-resources! [context config]
  (let [metadata (. context getMetaData)] 
    (loop [urls (.. config getClass getClassLoader getURLs)]
      (when-not (empty? urls)
        (do (. metadata addContainerResource
                        (Resource/newResource (first urls)))
            (recur (rest urls)))))))

(defn- create-handler [app]
  (let [config (AnnotationConfiguration.)]
    (doto (WebAppContext.)
      (add-container-resources! config)
      (.setConfigurations
        (into-array Configuration [config]))
      (.addServlet 
        (doto (ServletHolder. (servlet app)) (.setName "default")) 
        "/"))))

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

;adapter for library use

(defn wrap-with-spitter [app]
  (wrap-with-logger app {:logger spitter}))

(defn spring-jetty-run [app options]
  (run-jetty-wrapper app build-spring-cfg options))
                         
