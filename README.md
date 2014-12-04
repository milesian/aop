# milesian/aop

This clojure library lets you wrap your stuartsierra components in the same way as AOP does.

In this project you can find aop actions and component matchers that are thought to fit in 
[milesian/BigBang](https://github.com/milesian/BigBang)

#### Releases and Dependency Information


```clojure
[milesian/aop "0.1.2-SNAPSHOT"]
```

```clojure
:dependencies [[org.clojure/clojure "1.6.0"]
               [com.stuartsierra/component "0.2.2"]
               [tangrammer/defrecord-wrapper "0.1.4-SNAPSHOT"]]
```

## Usage
To get how this milesian/aop works on stuartsierra components you should start reading the internal tool used [tangrammer/defrecord-wrapper](https://github.com/tangrammer/defrecord-wrapper) due that we are actually wrapping defrecords that will behave as components. And after that, take a look at [milesian/BigBang](https://github.com/milesian/BigBang) to know how milesian.aop/wrap action is plugged into your system-start process


## Let's match with component perspective 

milesian/aop include a [tangramer.defrecord-wrapper/Match](https://github.com/tangrammer/defrecord-wrapper/blob/master/src/defrecord_wrapper/aop.clj#L4-L5) implementation that **use a stuartsierra/component perspective in contrast to** function and protocol perspective of tangrammer/defrecord-wrapper [SimpleProtocolMatcher](https://github.com/tangrammer/defrecord-wrapper/blob/master/src/defrecord_wrapper/aop.clj#L15) implementation.

####  ComponentMatcher 
This implementation  uses the name (system-map key) of the component in the system and try to match using its component protocols.
For example if we take an example from [milesian/system-examples](https://github.com/milesian/system-examples/blob/master/src/milesian/system_examples.clj), the :c component implements Talk protocol, so if we write the following clojure code, all the Talk protocol function implementations of our :c component will be wrapped by [milesian.aop.utils/logging-function-invocation](https://github.com/milesian/aop/blob/master/src/milesian/aop/utils.clj#L20) 

```clojure

;; following milesian/BigBang system start pattern, we need to 
;; include a milesian.aop/wrap action with a matcher 
;; (in this case using ComponentMatcher impl)

 [milesian.aop/wrap (milesian.aop.matchers/new-component-matcher 
                                          :system system-map 
                                          :components [:c] 
                                          :fn milesian.aop.utils/logging-function-invocation)]                                          
```

###  Dependency Component Query Oriented 
This project also contains two ComponentMatcher function constructors that let you query/filter the components to match using a dependency component query point of view. 


#### ComponentTransitiveDependenciesMatcher fn constructor
**[new-component-transitive-dependencies-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L33)** uses [stuartsierra/dependency transitive-dependencies](https://github.com/stuartsierra/dependency/blob/master/src/com/stuartsierra/dependency.clj#L19)  to get the dependencies fo each component specified in ```:components [...]``` argument , so if you use same example project and changes fn constructor, passing :c component, you'll have matched following components :a :b :c, due that [:c depends on :b and :b depends on :a besides :c also depends on :a](https://github.com/milesian/system-examples/blob/master/src/milesian/system_examples.clj#L45-L50)

```clojure
  (milesian.aop.matchers/new-component-transitive-dependencies-matcher 
                                          :system system-map 
                                          :components [:c] 
                                          :fn milesian.aop.utils/logging-function-invocation)
 ;; it's the same as                                           
 
   (milesian.aop.matchers/new-component-matcher 
                                          :system system-map 
                                          :components [:a :b :c] 
                                          :fn milesian.aop.utils/logging-function-invocation)
 
```

 
#### ComponentTransitiveDependentsMatcher fn constructor
**[new-component-transitive-dependents-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L40)** uses [stuartsierra/dependency transitive-dependents](https://github.com/stuartsierra/dependency/blob/master/src/com/stuartsierra/dependency.clj#L22) to get the dependents components for each component specified in ```:components [...]``` argument, then if you use same example project and changes fn constructor, passing :a component you'll have matched following components :a :b :c, due that [:c depends on :b and :b depends on :a besides :c also depends on :a](https://github.com/milesian/system-examples/blob/master/src/milesian/system_examples.clj#L45-L50)
```clojure
  (milesian.aop.matchers/new-component-transitive-dependents-matcher 
                                          :system system-map 
                                          :components [:a] 
                                          :fn milesian.aop.utils/logging-function-invocation)
 ;; it's the same as                                           
 
   (milesian.aop.matchers/new-component-matcher 
                                          :system system-map 
                                          :components [:a :b :c] 
                                          :fn milesian.aop.utils/logging-function-invocation)
 
```


## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the [MIT License](http://opensource.org/licenses/MIT). This means that pieces of this library may be copied into other libraries if they don't wish to have this as an explicit dependency, as long as it is credited within the code.

Copyright "Hesperidium" image @ [clipart](http://etc.usf.edu/clipart/)

