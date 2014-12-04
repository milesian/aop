(ns milesian.aop.matchers
  (:require [defrecord-wrapper.aop :refer (Matcher)]
            [defrecord-wrapper.reflect :as r]
            [com.stuartsierra.dependency :as dep]
            [com.stuartsierra.component :as component]
            [clojure.set :refer (superset?)]))

(defrecord ComponentMatcher [system components fn]
  Matcher
  (match [this protocol function-name function-args]
    (let [protocols (reduce #(into % (r/get-protocols (get system %2))) #{} components)]
      (when (contains? protocols protocol)
       fn))))

(defn new-component-matcher [& {:as opts}]
  (->> opts
       map->ComponentMatcher))

(defn- dependency-graph [system]
  (component/dependency-graph system (keys system)))

(defn- strict-deps
  "get only dependencies uniques"
  [system component-key]
  (let [dep-graph (dependency-graph system)
        dependencies (dep/transitive-dependencies dep-graph component-key)]
    (->>  (map (fn [k]
                 (let [d (dep/transitive-dependents dep-graph k)]
                   (when (superset? (conj  dependencies component-key) d)
                     k))) dependencies)
          (remove nil?))))

(defn new-component-transitive-dependencies-matcher [& {:as opts}]
  (-> opts
      (merge {:components (apply conj
                                 (vec (mapcat (partial strict-deps (:system opts)) (:components opts)))
                                 (:components opts))})
       map->ComponentMatcher))

(defn new-component-transitive-dependents-matcher [& {:as opts}]
  (let [dep-graph (dependency-graph (:system opts))
        transitive-dependents (fn [k] (dep/transitive-dependents dep-graph k))]
    (-> opts
       (merge {:components (apply conj
                                  (vec (mapcat transitive-dependents (:components opts)))
                                  (:components opts))})
       map->ComponentMatcher)))
