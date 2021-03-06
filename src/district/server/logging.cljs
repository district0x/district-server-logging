(ns district.server.logging
  (:require
   [cljs-node-io.core :as io :refer [spit]]
   [cljs-node-io.file :refer [File]]
   [cljs.nodejs :as nodejs]
   [clojure.pprint :as pprint]
   [clojure.set :as set]
   [clojure.string :as string]
   [district.server.config :refer [config]]
   [district.shared.error-handling :refer [error?]]
   [mount.core :as mount :refer [defstate]]
   [taoensso.timbre :as timbre]
   [cljs-time.core :as t]
   [district.format :as format]))

(def Sentry (nodejs/require "@sentry/node"))
(def Chalk (nodejs/require "chalk"))

(def ^:private timbre->sentry-levels
  {:trace  "debug"
   :debug  "debug"
   :info   "info"
   :warn   "warning"
   :error  "error"
   :fatal  "fatal"
   :report "info"})

(defn- decode-vargs [vargs]
  (reduce (fn [m arg]
            (assoc m (cond
                       (qualified-keyword? arg) :log-ns
                       (string? arg) :message
                       (map? arg) :meta) arg))
          {}
          vargs))

(defn- logline [data]
  (-> data
      (select-keys [:level :?ns-str :?file :?line :message :meta :instant])
      (set/rename-keys {:instant :timestamp})))

(defn console-logline [data]
  (let [{:keys [:level :log-ns :?ns-str :?file :?line :message :meta :timestamp]} (logline data)
        {:keys [:ns :line :file]} meta]
    (string/join " "
                 [((.keyword Chalk (case level
                                     :debug "cyan"
                                     :info "cyan"
                                     :warn "yellow"
                                     :error "red"
                                     "red"))
                   (.bold Chalk (string/upper-case (name level))))
                  (.bold Chalk (str message
                                    (when meta
                                      (.reset Chalk (str "\n" (with-out-str (pprint/pprint meta)))))))
                  (.bold Chalk "in")
                  (str
                   (.reset Chalk (or log-ns ns ?ns-str))
                   "["
                   (or file ?file)
                   ":"
                   (or line ?line)
                   "]")
                  (.bold Chalk "at")
                  (.white Chalk timestamp)])))

(defn console-appender []
  {:enabled? true
   :async? false
   :min-level nil
   :rate-limit nil
   :output-fn nil
   :fn (fn [data]
         (print (console-logline data)))})

(defn file-appender
  [{:keys [path roll-daily?] :as options}]
  (let [today-str (fn [] (format/format-date (t/now) :basic-date))
        new-state (fn [] {:file (File. path)
                          :for-day (today-str)})
        state (atom (new-state))
        write-to-file (fn [file line]
                        (spit file (str line "\n") :append (.exists file)))]
    {:enabled? true
     :async? false
     :min-level nil
     :rate-limit nil
     :output-fn nil
     :fn (fn [data]
           (let [{:keys [file for-day]} @state
                 today (today-str)]

             (when (and roll-daily? (not= for-day today))
               (.renameTo file (str path "-" for-day))
               (reset! state (new-state)))

             (write-to-file (:file @state) (logline data))))}))

(defn sentry-appender [{:keys [:min-level]}]
  {:enabled? true
   :async? true
   :min-level (or min-level :warn)
   :rate-limit nil
   :output-fn :inherit
   :fn (fn [{:keys [:level :?ns-str :?line :message :meta :log-ns] :as data}]
         (let [{:keys [:error :user :ns :line]} meta]
           (when meta
             (-> Sentry (.configureScope (fn [scope]
                                           (doseq [[k v] meta]
                                             (-> scope (.setExtra (name k) (clj->js v))))
                                           (when user
                                             (-> scope (.setUser (clj->js user))))))))
           (if (error? error) ;; check for js/Error to avoid syntheticException, see https://docs.sentry.io/platforms/node/#hints-for-events
             (-> Sentry (.captureException error))
             (-> Sentry (.captureEvent (clj->js {:level (timbre->sentry-levels level)
                                                 :message (or message error)
                                                 :logger (str (or log-ns ns ?ns-str) ":" (or line ?line))}))))))})

(defn wrap-decode-vargs [data]
  "Middleware for vargs"
  (merge data (decode-vargs (-> data
                                :vargs))))

(defn start [{:keys [:level :console? :file :sentry]}]
  (when sentry
    (.init Sentry (clj->js sentry)))
  (timbre/merge-config!
   {:level (keyword level)
    :middleware [wrap-decode-vargs]
    :appenders {:console (when console?
                           (console-appender))
                :file (when file
                        (file-appender file))

                :sentry (when sentry
                          (sentry-appender sentry))}}))

(defstate logging :start (start (merge (:logging @config)
                                       (:logging (mount/args)))))
