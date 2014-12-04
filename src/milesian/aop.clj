(ns milesian.aop
  (:require [defrecord-wrapper.aop :refer (add-extends Matcher)]
            [defrecord-wrapper.reflect :as r]
            [com.stuartsierra.dependency :as dep]
            [com.stuartsierra.component :as component]
            [clojure.set :refer (difference)])
  (:import [defrecord_wrapper.aop  SimpleWrapper ]))

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
    (do
      (add-extends SimpleWrapper (r/get-specific-supers c*) matcher)
      (-> (with-meta (SimpleWrapper. c*) (meta c*))
          (merge c*)))
    c*))
