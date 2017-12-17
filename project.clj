(defproject district0x/district-server-logging "1.0.0"
  :description "district0x server component for setting up logging"
  :url "https://github.com/district0x/district-server-logging"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[district0x/district-server-config "1.0.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojurescript "1.9.946"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/encore "2.92.0"]]

  :npm {:dependencies [[chalk "2.3.0"]]})
