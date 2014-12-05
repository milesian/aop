(ns milesian.aop.utils
  (:require [clojure.string :as st]))

(defn extract-data
  [*fn* this args]
  (let [component-key (:bigbang/key (meta this))
        [fn-name fn-args]((juxt :function-name :function-args) (meta *fn*))
        who (:bigbang/who (meta (:wrapper (meta *fn*))))
        formatted-args (-> (st/replace (str fn-args) #"\[" "")
                                           (st/replace #"\]" "")
                                           (st/split #" ")
                                           (next)
                                           ((partial st/join " " )))]
    {:who (if-not (nil? who) (name who) "REPL")
     :id (name component-key)
     :fn-name fn-name
     :fn-args formatted-args}))

(defn function-invocation
  [*fn* this args]
  (let [{:keys [id who fn-name fn-args]} (extract-data *fn* this args)]
    (format "%s->%s: %s %s" who id fn-name fn-args)))

(defn function-return
  [*fn* this args]
  (let [{:keys [id who fn-name fn-args]} (extract-data *fn* this args)]
    (format "%s->%s:" id who)))


(defn logging-function-invocation
  [*fn* this & args]
  (println (function-invocation *fn* this args))
  (apply *fn* (conj args this)))
