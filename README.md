# district-server-logging

Clojurescript-node.js [mount](https://github.com/tolitius/mount) component for a district server, that takes care of logging. This component currently utilises [timbre](https://github.com/ptaoussanis/timbre) as a logging library.

## Installation
Add `[district0x/district-server-logging "1.0.0"]` into your project.clj  
Include `[district.server.logging]` in your CLJS file, where you use `mount/start`

**Warning:** district0x components are still in early stages, therefore API can change in a future.

## Real-world example
To see how district server components play together in real-world app, you can take a look at [NameBazaar server folder](https://github.com/district0x/name-bazaar/tree/master/src/name_bazaar/server), 
where this is deployed in production.

## Usage
You can pass following args to logging component: 
* `:level` Min. level that should be logged
* `:console?` Pass true if you want to log into console as well
* `:file-path` Absolute path to log file

```clojure
  (ns my-district
    (:require [mount.core :as mount]
              [district.server.logging]
              [taoensso.timbre :refer-macros [info warn error]]))

  (-> (mount/with-args
        {:logging {:level :info
                   :console? true}})
    (mount/start))

  (info "Some info")
  ;; INFO [my-district:12] - Some info

  (warn "Some warning" {:a 1})
  ;; WARN [my-district:15] - Some warning {:a 1}

  (error "Some error" {:error "Bad things"})
  ;; ERROR [my-district:18] - Some error {:error "Bad things"}
```

## Component dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-logging` can get initial args from config provided by `district-server-config/config` under the key `:logging`. These args are then merged together by ones passed to `mount/with-args`.

If you wish to use custom components instead of dependencies above while still using `district-server-logging`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).