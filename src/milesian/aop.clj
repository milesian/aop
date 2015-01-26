(ns milesian.aop
  (:require [defrecord-wrapper.aop :refer (add-extends Matcher)]
            [defrecord-wrapper.reflect :as r]
            [com.stuartsierra.dependency :as dep]
            [com.stuartsierra.component :as component]
            [clojure.set :refer (difference)]))


;;http://grokbase.com/t/gg/clojure/11cjvz1jda/defrecord-based-on-runtime-metadata
;;http://stackoverflow.com/questions/3748559/clojure-creating-new-instance-from-string-class-name

(defn record-factory [recordname]
  (let [recordclass ^Class (resolve (symbol recordname))
        max-arg-count (apply max (map #(count (.getParameterTypes %))
                                      (.getConstructors recordclass)))
        args (map #(symbol (str "x" %)) (range (- max-arg-count 2)))]
    (eval `(with-meta (fn [~@args] (new ~(symbol recordname) ~@args)) {:record-class ~recordclass}))))


(def wrappers  (atom (map #(do (eval `(do
                                   (defrecord ~(symbol (str "Wrapper-" %)) [~'wrapped-record])))
                          (record-factory (str "Wrapper-" %))) (range))))


(defn is-defrecord-instance? [instance]
  (empty? (difference #{java.lang.Iterable clojure.lang.Counted clojure.lang.Seqable
                                    clojure.lang.IKeywordLookup clojure.lang.Associative clojure.lang.IObj
                                    clojure.lang.IMeta java.io.Serializable clojure.lang.IPersistentCollection
                                    clojure.lang.IHashEq clojure.lang.IPersistentMap clojure.lang.IRecord
                                    java.util.Map clojure.lang.ILookup java.lang.Object}
                                  (supers (class instance)))))

(defn ^{:bigbang/phase :on-start} wrap
  "wrap component using matcher to apply middleware to original fns"
  [c* matcher]
  (if (is-defrecord-instance? c*) ;; not all components are defrecords
    (let [r (-> wrappers deref first)]
      (add-extends (eval (-> r meta :record-class)) (r/get-specific-supers c*) matcher)
      (let [res  (-> (with-meta (r c*) (meta c*))
                     (merge c*))]
        (swap! wrappers (partial drop 1))
        res))
    c*))
