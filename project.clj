(defproject district0x/district-server-logging "1.0.6-SNAPSHOT"
  :description "district0x server module for setting up logging"
  :url "https://github.com/district0x/district-server-logging"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[district0x/district-server-config "1.0.1"]
                 [district0x/error-handling "1.0.3"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.10.520"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/encore "2.92.0"]
                 [district0x/district-format "1.0.8"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]]

  :npm {:dependencies [[chalk "2.3.0"]
                       ["@sentry/node" "4.2.1"]]
        :devDependencies [[ws "2.0.1"]]}

  :figwheel {:server-port 4661}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [cider/piggieback "0.4.0"]
                                  [figwheel-sidecar "0.5.18"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-figwheel "0.5.14"]
                             [lein-npm "0.6.2"]
                             [lein-doo "0.1.7"]]
                   :source-paths ["dev"]}}

  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["deploy"]]

  :cljsbuild {:builds [{:id "tests"
                        :source-paths ["src" "test"]
                        :figwheel {:on-jsload "tests.runner/-main"}
                        :compiler {:main "tests.runner"
                                   :output-to "tests-compiled/run-tests.js"
                                   :output-dir "tests-compiled"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true}}]})
