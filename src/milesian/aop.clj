(ns milesian.aop
  (:require [defrecord-wrapper.aop :refer (add-extends Matcher)]
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

(defn ^{:bigbang/phase :on-start} wrap
  "wrap component using matcher to apply middleware to original fns"
  [c* matcher]
  (if (is-defrecord-instance? c*) ;; not all components are defrecords
    (do
      (add-extends SimpleWrapper (r/get-specific-supers c*) matcher)
      (-> (with-meta (SimpleWrapper. c*) (meta c*))
          (merge c*)))
    c*))


(defrecord ComponentMatcher [system components fn]
  Matcher
  (match [this protocol function-name function-args]
    (let [protocols (reduce #(into % (r/get-protocols (get system %2))) #{} components)]
      (when (contains? protocols protocol)
       fn))))

(defn new-component-matcher [& {:as opts}]
  (->> opts
       map->ComponentMatcher))



(defrecord Banana [qty])
(defrecord Grape  [qty])
(defrecord Orange [qty])

;;; 'subtotal' differs from each fruit.

(defprotocol Fruit
  (subtotal [item]))

(extend-type Banana
  Fruit
  (subtotal [item]
    (* 158 (:qty item))))

(extend-type Grape
  Fruit
  (subtotal [item]
    (* 178 (:qty item))))

(extend-type Orange
  Fruit
  (subtotal [item]
    (* 98 (:qty item))))
(defn coupon [item]
  (reify Fruit
    (subtotal [_]
      (int (* 0.75 (subtotal item))))))
(r/get-specific-supers (coupon (Banana. 3)))
