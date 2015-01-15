(defproject milesian/aop "0.1.3-SNAPSHOT"
  :description "milesian bigbang AOP actions"
  :url "https://github.com/milesian/aop"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.stuartsierra/component "0.2.2"]
                 [tangrammer/defrecord-wrapper "defrecord-wrapper-0.1.5-20150115.113546-1"]]
  :profiles {:dev {:dependencies [[milesian/system-examples "0.1.1"]]}}
  )
