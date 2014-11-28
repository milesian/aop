# epi-component

![image](https://dl.dropboxusercontent.com/u/8688858/epicarp.gif)

This clojure library lets you wrap your stuartsierra components in the same way as AOP does
It's heavily based on [tangrammer/defrecord-wrapper](https://github.com/tangrammer/defrecord-wrapper) 

#### Releases and Dependency Information


```clojure
[milesian/epi-component "0.1.1"]
```

```clojure
:dependencies [[org.clojure/clojure "1.6.0"]
               [com.stuartsierra/component "0.2.2"]
               [tangrammer/defrecord-wrapper "0.1.4"]]
```


## Usage

Working with stuartsierra/component library enforces us to use [indirectly](https://github.com/stuartsierra/component/blob/master/src/com/stuartsierra/component.clj#L143-L151) or directly [component/update-system](https://github.com/stuartsierra/component/blob/master/src/com/stuartsierra/component.clj#L117) to start our system.    
It's so important this fn in stuartsierra/component library that I'll copy here the doc of ```component/update-system```
```clojure 
  "Invokes (apply f component args) on each of the components at
  component-keys in the system, in dependency order. Before invoking
  f, assoc's updated dependencies of the component."
```

To use ```tangrammer/epi-component``` we'll use ```component/update-system``` to intercept each component system

```clojure
(ns your-app.your-ns
(:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [tangrammer.epi-component.aop :refer (intercept)]))

;; define your system-map as normally you do, or follow this basic system https://github.com/tangrammer/epi-component/blob/master/test/tangrammer/epi_component/aop_test.clj#L9-L69            

;; once you get your system definition function
(defn system-1 []
  (map->System1 {:a (-> (component-a))
                 :b (-> (component-b)
                        (component/using [:a]))
                 :c (-> (component-c)
                        (component/using [:a :b]))
                 :d (-> (component-d)
                        (component/using {:b :b :my-c :c}))
                 :e (component-e)}))
                 
;; define a middleware protocol fucntion Matcher https://github.com/tangrammer/defrecord-wrapper/blob/master/src/defrecord_wrapper/aop.clj#L4 
;; or just use a function to apply to all functions protocols

(defn logging-access-protocol
  [*fn* this & args]
  (println ">> LOGGING-ACCESS " [this args])
  (println ">>"(meta *fn*))
  (apply *fn* (conj args this)))


;; intercept your system 

(def system (component/update-system (system-1) (keys (system-1))
                                     (comp
                                      #(intercept % logging-access-protocol)
                                      component/start)))

;; invoke your components functions protocols

(listening (:b system))
;;=> >> LOGGING-ACCESS  [#your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}} nil]
;;=> >> {:function-args [_], :wrapper #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state B}, :function-name listening}



(talking (:c system))
;;=> >> LOGGING-ACCESS  [#your_app.your_ns.ComponentC{:state state C, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :b #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state B}} nil]
;;=> >> {:function-args [_], :wrapper #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentC{:state state C, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :b #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state B}}, :b #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state B}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state C}, :function-name talking}
;;=> >> LOGGING-ACCESS  [#your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}} nil]
;;=> >> {:function-args [_], :wrapper #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentB{:state state B, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}}, :a #defrecord_wrapper.aop.SimpleWrapper{:wrapped-record #your_app.your_ns.ComponentA{:state state A}, :state state A}, :state state B}, :function-name listening}
                 
            
```


### Matcher implementation
So far, [tangrammer/bidi-wrapper-matcher](https://github.com/tangrammer/bidi-wrapper-matcher) is an implementation of defrecord-wrapper.aop.Matcher protocol. As its name means, it's based on [bidi](https://github.com/juxt/bidi)  pattern matching lib. 


## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the [MIT License](http://opensource.org/licenses/MIT). This means that pieces of this library may be copied into other libraries if they don't wish to have this as an explicit dependency, as long as it is credited within the code.

Copyright "Hesperidium" image @ [clipart](http://etc.usf.edu/clipart/)
