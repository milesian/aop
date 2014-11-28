;; this fns thought to be applied at the component/update-system call that we user component/start
(ns milesian.epi-component
  (:require [defrecord-wrapper.aop :refer (add-extends)]
            [defrecord-wrapper.reflect :as r]
            [com.stuartsierra.component :as component ])
  (:import [defrecord_wrapper.aop  SimpleWrapper ]))

(defn is-defrecord-instance? [instance]
  (empty? (clojure.set/difference #{java.lang.Iterable clojure.lang.Counted clojure.lang.Seqable
                                    clojure.lang.IKeywordLookup clojure.lang.Associative clojure.lang.IObj
                                    clojure.lang.IMeta java.io.Serializable clojure.lang.IPersistentCollection
                                    clojure.lang.IHashEq clojure.lang.IPersistentMap clojure.lang.IRecord
                                    java.util.Map clojure.lang.ILookup java.lang.Object}
                                  (supers (class instance)))))


(defn ^{:bigbang/phase :on-start} assoc-meta-who-to-deps
  "this fn lets you store into component dependencies the parent component key"
  [c* & _]

  (assert (not (nil? (:bigbang/key (meta c*))))
          "this fn needs your components meta tag with :api-component/key")
  (let [component-key (:bigbang/key (meta c*))]
    (reduce (fn [c [dep-key _]]
              (let [dep (get c dep-key)]
                (let [new-dep (vary-meta dep assoc :bigbang/who component-key)]
                  (assoc c dep-key new-dep))))
            c*
            (component/dependencies c*))))

(defn ^{:bigbang/phase :on-start} wrap
  "wrap component using matcher to apply middleware to original fns"
  [c* matcher]
  (if (is-defrecord-instance? c*) ;; not all components are defrecords
    (do
      (add-extends SimpleWrapper (r/get-specific-supers c*) matcher)
      (-> (with-meta (SimpleWrapper. c*) (meta c*))
          (merge c*)))
    c*))
