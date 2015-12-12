(defproject org.springframework.security.demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.4.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-logger "0.7.5"]
                 [com.taoensso/timbre "4.1.4"]
                 [org.springframework/spring-beans "4.2.3.RELEASE"]
                 [org.springframework.security/spring-security-web
                   "4.0.3.RELEASE"]
                 [org.springframework.security/spring-security-config
                   "4.0.3.RELEASE"]
                 [org.eclipse.jetty/jetty-annotations  "9.2.14.v20151106"]
                 [org.eclipse.jetty/jetty-server  "9.2.14.v20151106"]
                 [org.eclipse.jetty/jetty-servlet "9.2.14.v20151106"]
                 [org.eclipse.jetty/jetty-util "9.2.14.v20151106"]
                 [org.eclipse.jetty/jetty-webapp "9.2.14.v20151106"]]
  :exclusions [javax.servlet/servlet-api]
  :aot :all
  :main org.springframework.security.demo.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
