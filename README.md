![image](https://dl.dropboxusercontent.com/u/8688858/epicarp-small.gif)

# milesian/aop

This clojure library lets you wrap your stuartsierra components with a component perspective as AOP does.

#### Releases and Dependency Information


```clojure
[milesian/aop "0.1.4"]
```

```clojure
:dependencies [[org.clojure/clojure "1.6.0"]
               [com.stuartsierra/component "0.2.2"]
               [tangrammer/defrecord-wrapper "0.1.6"]]
```

[milesian/aop](https://github.com/milesian/aop)  lets you wrap your stuartsierra/components in the same way as AOP does. 

... for those who aren't familiar with [AOP](http://en.wikipedia.org/wiki/Aspect-oriented_programming), it is a programming paradigme that aims to increase modularity by allowing the separation of cross-cutting concerns. Examples of cross-cutting concerns can be: applying security, logging and throwing events, and as wikipedia explains:
> Logging exemplifies a crosscutting concern because a logging strategy necessarily affects every logged part of the system. Logging thereby crosscuts all logged classes and methods....


### what is it included?
It includes a **wrap function** that works as a customization system function and specific **component-matchers** to calculate the-component-place where we'll apply middleware.

### basic howto 
To simplify AOP meanings, let's try *refactoring* for a while two AOP concepts to quickly understand the functionality provided.

+ the **thing-to-happen** = aspect/cross-cutting concern
+ the **place-where-will-happen** = target


So, basically to include a new **thing-to-happen** in your component system, you need to define the **thing-to-happen** and the **place-where-will-happen**

   

#### **thing-to-happen**
It's **a function milddleware**, very similar to [common ring middleware](https://github.com/ring-clojure/ring/wiki/Concepts#middleware)

```clojure
(defn your-fn-middleware
  [*fn* this & args]
  (let [fn-result (apply *fn* (conj args this))]
   fn-result))
```
   
#### **place-where-will-happen**
It's calculated with a [defrecord-wrapper.aop/Matcher](https://github.com/tangrammer/defrecord-wrapper/blob/master/src/defrecord_wrapper/aop.clj#L4) protocol implementation

```clojure
(defprotocol defrecord-wrapper.aop/Matcher
  (match [this protocol function-name function-args]))
```



### Minimal example
As you can see the options available to decide if the **thing** has to happen in current **place** are component protocol, function-name and function-args
Let's try to use this AOP stuff in a minimal example: 


#### 1. define protocols and component 
```clojure
(defprotocol Database
  (save-user [_ user])
  (remove-user [_ user]))

(defprotocol WebSocket
  (send [_ data]))

(defrecord YourComponent []
  Database
  (save-user [this user]
    (format "saving user: %" user ))
  (remove-user [this user]
    (format "removing user: %" user ))
  Websocket
  (send [this data]
    (format "sending data: %" data)))
   
```
#### 2. define your middleware to apply (thing-to-happen)

```clojure
(defn logging-middleware
  [*fn* this & args]
  (let [fn-result (apply *fn* (conj args this))]
   (println "aop-logging/ function-name:" (:function-name (meta *fn*)))
   fn-result))
```

#### 3. define your matcher (place-where-will-happen)

```clojure
;; maybe you want match all your component fns protocols

(defrecord YourComponentMatcher [middleware]
  defrecord-wrapper.aop/Matcher
  (match [this protocol function-name function-args]
    (when (contains? #{Database WebSocket} protocol))
    middleware))
    

;; or maybe you're only are interested in Database/remove-user function

(defrecord YourRefinedComponentMatcher [middleware]
  defrecord-wrapper.aop/Matcher
  (match [this protocol function-name function-args]
    (when (and (= Database protocol) (= function-name "remove-user")))
    middleware))
```



#### 4. wrap your system (apply conditional middleware to your components)
 
```clojure
;;  construct your instance of SystemMap as usual
(def system-map (component/system-map :your-component (YourComponent.)))

;; Using stuartsierra customization way
(def started-system (-> system-map
                        (component/update-system 
                         (comp component/start 
                               #(milesian.aop/wrap % (YourRefinedComponentMatcher. logging-middleware))))))
  
  ;; or, if you prefer a better way to express the same
  ;; you can use milesian/BigBang
(def started-system (milesian.bigbang/expand
                     system-map
                     {:before-start []
                      :after-start  [[milesian.aop/wrap (YourRefinedComponentMatcher. logging-middleware)]]}))

```
#### 5. try your wrapped-started-system
 
```clojure
;;  construct your instance of SystemMap as usual
(-> started-system :your-component (send "data"))
=> repl output: aop-logging/ function-name: send

```


## Let's match with component perspective 

milesian/aop includes a [Matcher](https://github.com/tangrammer/defrecord-wrapper/blob/master/src/defrecord_wrapper/aop.clj#L4-L5) implementation that **uses a stuartsierra/component perspective in contrast to** function and protocol perspective of [matchers](https://github.com/tangrammer/defrecord-wrapper/blob/master/README.md#matchers-available-in-tangrammerdefrecord-wrapper) included on more generic tangrammer/defrecord-wrapper lib
Also offers a simple "Dependency Component Query Oriented" that I found very useful to think/query the system in a component way :- in our component case is the same as straighforward way 

####  ComponentMatcher 
This implementation  uses the system component-id to match using its component protocols and the middleware fn to apply.

Example using previous example will match both protocols: Database and Websocket, and therefore all their related fns. Previous matchers examples used protocols and fn-names to do their works, now we are at a high level, a component level.

```clojure

(milesian.aop.matchers/new-component-matcher :system system-map 
                                             :components [:your-component] 
                                             :fn logging-middleware)]                                          
```

###  Dependency Component Query Oriented 
This project also contains two ComponentMatcher function constructors that let you match using a dependency component query point of view. 

Let's extend our data example adding a couple of components more:

```clojure
(defprotocol Greetings
  (morning [_]))

(defrecord GreetingsComponent [your-component]
  Greetings
  (morning [this]
    (send your-component "Morning, it's a great day here!"))

(defprotocol Connector
  (connect [_]))

(defrecord ConnectorComponent [greetings-component]
  Connector
  (connect [this]
    (morning greetings-component))
```
And also we'll need extend our system definition

```clojure
(def system-map (component/system-map   
                 :your-component (YourComponent.)
                 :greetings-components (->(GreetingsComponent.)
                                          (component/using [:your-component]))
                 :connector-component (->(ConnectorComponent.)
                                         (component/using [:connector-component]))))

```

#### ComponentTransitiveDependenciesMatcher fn constructor
**[new-component-transitive-dependencies-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L33)** uses [stuartsierra/dependency transitive-dependencies](https://github.com/stuartsierra/dependency/blob/master/src/com/stuartsierra/dependency.clj#L19) to get all component dependencies for each component specified in `:components [...]` argument. 

```clojure
(milesian.aop.matchers/new-component-transitive-dependencies-matcher 
 :system system-map 
 :components [:your-component] 
 :fn logging-middleware)
;; it's the same as                                           

(milesian.aop.matchers/new-component-matcher 
 :system system-map 
 :components [:your-component :greetings-component :connector-component] 
 :fn logging-middleware)
 
```

 
#### ComponentTransitiveDependentsMatcher fn constructor
**[new-component-transitive-dependents-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L40)** uses [stuartsierra/dependency transitive-dependents](https://github.com/stuartsierra/dependency/blob/master/src/com/stuartsierra/dependency.clj#L22) to get the all dependents components for each component specified in `:components [...]` argument.

```clojure
(milesian.aop.matchers/new-component-transitive-dependents-matcher 
 :system system-map 
 :components [:connector-component] 
 :fn logging-middleware)
;; it's the same as                                           

(milesian.aop.matchers/new-component-matcher 
 :system system-map 
 :components [:your-component :greetings-component :connector-component] 
 :fn logging-middleware)
 
```




## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the [MIT License](http://opensource.org/licenses/MIT). This means that pieces of this library may be copied into other libraries if they don't wish to have this as an explicit dependency, as long as it is credited within the code.

Copyright "Hesperidium" image @ [clipart](http://etc.usf.edu/clipart/)

