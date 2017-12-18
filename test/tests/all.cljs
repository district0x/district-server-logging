(ns tests.all
  (:require
    [cljs.test :refer-macros [deftest is testing use-fixtures]]
    [district.server.logging]
    [mount.core :as mount]
    [taoensso.timbre :as timbre]))

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(deftest test-logging
  (-> (mount/with-args
        {:logging {:level :warn
                   :console? true
                   :file-path "/tmp/my-log.log"}})
    (mount/start))

  (let [{:keys [:level :appenders]} timbre/*config*]
    (is (= :warn level))
    (is (true? (:enabled? (:console appenders))))
    (is (true? (:enabled? (:file appenders))))))
