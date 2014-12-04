(ns milesian.aop.utils
  (:require [clojure.string :as st]))

(defn function-invocation
  [*fn* this args]
  (let [component-key (:bigbang/key (meta this))
        [fn-name fn-args]((juxt :function-name :function-args) (meta *fn*))
        who (:bigbang/who (meta (:wrapper (meta *fn*))))
        formatted-args (-> (st/replace (str fn-args) #"\[" "")
                                           (st/replace #"\]" "")
                                           (st/split #" ")
                                           (next)
                                           ((partial st/join " " )))]
    (format "%s->%s: %s %s"
            (if-not (nil? who) (name who) "REPL")
            (name component-key)
            fn-name
            formatted-args)))

(defn logging-function-invocation
  [*fn* this & args]
  (println (function-invocation *fn* this args))
  (apply *fn* (conj args this)))
