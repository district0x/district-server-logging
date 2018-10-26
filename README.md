# district-server-logging

[![Build Status](https://travis-ci.org/district0x/district-server-logging.svg?branch=master)](https://travis-ci.org/district0x/district-server-logging)

Clojurescript-node.js [mount](https://github.com/tolitius/mount) module for a district server, that takes care of logging. This module currently utilises [timbre](https://github.com/ptaoussanis/timbre) as a logging library.

## Installation
Add `[district0x/district-server-logging "1.0.4-SNAPSHOT"]` into your project.clj  
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
* `:sentry` [sentry](https://sentry.io/) [configuration options](#sentry)

Log calls take the following arguments:

* `message` (required) string with a human-readable message.
* `meta` (optional) a map with context meta-data.
* `ns` (optional) namespaced keyword for easy aggregating and searching. If none provided the module will do it's best to figure out which namespace is the log call coming from. 

Example:

```clojure
  (ns my-district
    (:require [mount.core :as mount]
              [district.server.logging]
              [taoensso.timbre :as log]))

  (-> (mount/with-args
        {:logging {:level :info
                   :console? true}})
    (mount/start))

  (log/error "Some error" {:error "Bad things"} ::error)
  ;; ERROR Some error 
  ;; {:error "Bad things"}
  ;;  in :my-district/error[my-district.clj:18] at Tue Oct 23 2018 19:20:20 GMT+0200 (CEST)
```

### <a name="sentry"> sentry configuration options
In order to initialize sentry logging appender pass a map of options:

* `:dsn` (required) tells the SDK where to send the events.
* `:debug` (optional) set to `true` if you want to turn debug mode on and get some extra information if something goes wrong when sending the event. The default is `false`. 
* `:maxBreadcrumbs` (optional) controls the total amount of breadcrumbs that should be captured, default is 100.
* `:min-level` (optional) sets the minimal level of logging to sentry, `:warn` is the default. This setting overrides the timbres `:level` flag!

Example:

```clojure
(-> (mount/with-args
      {:logging {:level :info
                 :sentry {:dsn "https://4bb89c9cdae14444819ff0ac3bcba253@sentry.io/1306960"
                          :min-level :warn}}})
    (mount/start))
```

In order to get the most of sentry logging facilities the log call should pass inside its meta-data:

* `:error` the vanilla error object 
* `:user` the user meta-data such as `:id`, `:username` `:email` `:ip-address` etc.

Example:

```clojure
(log/error "foo" {:error (js/Error. "bad error") 
                  :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"
                  :username "Hans"
                  :email "user@district.com"
                  :ip-address "90.177.15.65"}} 
                 ::error)
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
