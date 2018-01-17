# district-server-logging

[![Build Status](https://travis-ci.org/district0x/district-server-logging.svg?branch=master)](https://travis-ci.org/district0x/district-server-logging)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) module for a district server, that takes care of logging. This module currently utilises [timbre](https://github.com/ptaoussanis/timbre) as a logging library.

## Installation
Add `[district0x/district-server-logging "1.0.1"]` into your project.clj  
Include `[district.server.logging]` in your CLJS file, where you use `mount/start`

**Warning:** district0x modules are still in early stages, therefore API can change in a future.

## Real-world example
To see how district server modules play together in real-world app, you can take a look at [NameBazaar server folder](https://github.com/district0x/name-bazaar/tree/master/src/name_bazaar/server), 
where this is deployed in production.

## Usage
You can pass following args to logging module: 
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

## Module dependencies

### [district-server-config](https://github.com/district0x/district-server-config)
`district-server-logging` gets initial args from config provided by `district-server-config/config` under the key `:logging`. These args are then merged together with ones passed to `mount/with-args`.

If you wish to use custom modules instead of dependencies above while still using `district-server-logging`, you can easily do so by [mount's states swapping](https://github.com/tolitius/mount#swapping-states-with-states).
## Development
```bash
# To start REPL and run tests
lein deps
lein repl
(start-tests!)

# In other terminal
node tests-compiled/run-tests.js

# To run tests without REPL
lein doo node "tests" once
```