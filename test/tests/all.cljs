(ns tests.all
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [cljs.nodejs :as nodejs]
   [district.server.logging]
   [mount.core :as mount]
   [taoensso.timbre :as log]
   [cljs-node-io.file :refer [File]]
   [cljs-time.core :as t]
   [district.server.logging :refer [file-appender]]
   [clojure.string :as str]
   [cljs-node-io.core :as io :refer [slurp]]))

(def path "/tmp/my-log.log")

(use-fixtures
  :each
  {:after
   (fn []
     (let [f (File. path)]
       (when (.exists f)
         (.delete f)))
     (mount/stop))})

(deftest test-logging
  (-> (mount/with-args
        {:logging {:level :warn
                   :console? true
                   :file {:path path}}})
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

(deftest file-appender-roll-test-1
  (testing "It should NOT roll when daily-roll? disabled"
    (let [now (atom (t/date-time 2019 1 1))]
      (with-redefs [t/now (fn [] @now)]
        (-> (mount/with-args
              {:logging {:level :warn
                         :file {:path path}}})
            (mount/start))

        ;; log something
        (log/error "foo" {:error "bad error" :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"}} ::error)

        ;; move to the next day
        (reset! now (t/date-time 2019 1 2))

        ;; log something else
        (log/error "bar" {:error "other bad error" :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"}} ::error)

        (is (= (-> (slurp path)
                   (str/split-lines)
                   count)
               2)
            "Since rolling is disable log file should contain 2 lines")))))

(deftest file-appender-roll-test-2
  (testing "It should roll when daily-roll? enabled"
    (let [now (atom (t/date-time 2019 1 1))
          old-file-path (str path "-20190101")]
      (with-redefs [t/now (fn [] @now)]
        (-> (mount/with-args
              {:logging {:level :warn
                         :file {:path path
                                :roll-daily? true}}})
            (mount/start))

        ;; log something
        (log/error "foo" {:error "bad error" :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"}} ::error)

        ;; move to the next day
        (reset! now (t/date-time 2019 1 2))

        ;; log something else
        (log/error "bar" {:error "other bad error" :user {:id "0xd1090C557909E2A91FA534d4F54dA82D47f3788e"}} ::error)

        (is (= (-> (slurp path)
                   (str/split-lines)
                   count)
               1)
            "Since rolling is enabled log file should contain 1 line")

        (is (= (-> (slurp old-file-path)
                   (str/split-lines)
                   count)
               1)
            "Since rolling is enabled a rolled file should exist and should contain 1 line")

        ))))
