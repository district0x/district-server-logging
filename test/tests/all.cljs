(ns tests.all
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [cljs.nodejs :as nodejs]
   [district.server.logging]
   [mount.core :as mount]
   [taoensso.timbre :as log]))

(def path "/tmp/my-log.log")

(use-fixtures
  :each
  {:after
   (fn []
     (mount/stop))})

(deftest test-logging
  (-> (mount/with-args
        {:logging {:level :warn
                   :console? true
                   :file-path path}})
      (mount/start))

  (let [fs (nodejs/require "fs")
        {:keys [:level :appenders]} log/*config*]
    (is (true? (:enabled? (:console appenders))))
    (is (true? (:enabled? (:file appenders))))
    (log/error "foo" {:error "bad error" :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"}} ::error)
    (.readFile fs path "utf8" (fn [err data]
                                (-> (cljs.reader/read-string (or err data))
                                    :message
                                    (#(is (= "foo" %))))))))
