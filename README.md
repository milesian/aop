# milesian/aop

![image](https://dl.dropboxusercontent.com/u/8688858/epicarp.gif)

This clojure library lets you wrap your stuartsierra components in the same way as AOP does

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


### Matchers implementation

Here you have a ComponentMatcher implementation, a component oriented matcher that uses the name of the component in the system and match using their protocols 

```

 [milesian.aop/wrap (milesian.aop.matchers/new-component-matcher :system system-map :components [:c] :fn milesian.aop.utils/logging-function-invocation)]

```

Also you can use stuartsierra/dependency fns to reduce your components to match. Following this idea you can find 2 more matchers [new-component-transitive-dependencies-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L33) and [new-component-transitive-dependents-matcher](https://github.com/milesian/aop/blob/master/src/milesian/aop/matchers.clj#L40)

## License

Copyright © 2014 Juan Antonio Ruz (juxt.pro)

Distributed under the [MIT License](http://opensource.org/licenses/MIT). This means that pieces of this library may be copied into other libraries if they don't wish to have this as an explicit dependency, as long as it is credited within the code.

Copyright "Hesperidium" image @ [clipart](http://etc.usf.edu/clipart/)
