# milesian/aop

![image](https://dl.dropboxusercontent.com/u/8688858/epicarp.gif)

This clojure library lets you wrap your stuartsierra components in the same way as AOP does
It's heavily based on [tangrammer/defrecord-wrapper](https://github.com/tangrammer/defrecord-wrapper) 

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

Take a look at [milesian/BigBang](https://github.com/milesian/BigBang)




### Matcher implementation
Here you have a ComponentMatcher implementation, a component oriented matcher that uses the name of the component in the system and match using their protocols 
```
;; ...
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
    (format "%s->%s: %s %s" (if-not (nil? who)
                              (name who)
                              "REPL"
                              ) (name component-key) fn-name formatted-args)))

(defn logging-function-invocation
  [*fn* this & args]
  (println (function-invocation *fn* this args))
  (apply *fn* (conj args this)))

;; ...

 [milesian.aop/wrap (milesian.aop/new-component-matcher :system system-map :components [:c] :fn logging-function-invocation)]

```

## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the [MIT License](http://opensource.org/licenses/MIT). This means that pieces of this library may be copied into other libraries if they don't wish to have this as an explicit dependency, as long as it is credited within the code.

Copyright "Hesperidium" image @ [clipart](http://etc.usf.edu/clipart/)
