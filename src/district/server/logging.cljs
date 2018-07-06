(ns district.server.logging
  (:require
    [cljs-node-io.core :as io :refer [spit]]
    [cljs-node-io.file :refer [File]]
    [cljs.nodejs :as nodejs]
    [clojure.pprint :as pprint]
    [clojure.set :as set]
    [clojure.string :as string]
    [district.server.config :refer [config]]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :as timbre]))

(declare start)

(defstate logging :start (start (merge (:logging @config)
                                       (:logging (mount/args)))))

(def chalk (nodejs/require "chalk"))

(defn console-logline [logdata]
  (-> logdata
      (select-keys [:instant :level :ns :message :meta :?file :?line])
      (set/rename-keys {:instant :timestamp}))
  (string/join " "
               [((.keyword chalk (case (:level logdata)
                                   :info "cyan"
                                   :warn "yellow"
                                   :error "red"
                                   "red"))
                 (.bold chalk (string/upper-case (name (:level logdata)))))
                (.bold chalk (str (:message logdata)
                                  (if-let [md (:meta logdata)]
                                    (.reset chalk (str "\n" (with-out-str (pprint/pprint md)))))))
                (.bold chalk "in")
                (if-let [meta-ns (get-in logdata [:meta :ns])]
                  (str
                   (.reset chalk meta-ns)
                   "["
                   (get-in logdata [:meta :file])
                   ":"
                   (get-in logdata [:meta :line])
                   "]")
                  (str
                   (.reset chalk (:ns logdata))
                   "["
                   (:?file logdata)
                   ":"
                   (:?line logdata)
                   "]"))                
                (.bold chalk "at")
                (.white chalk (:instant logdata))]))


(defn logline [logdata]
  (-> logdata
    (select-keys [:instant :level :ns :message :meta :?file :?line])
    (set/rename-keys {:instant :timestamp})))


(defn- decode-vargs [vargs]
  (reduce (fn [m arg]
            (assoc m (cond
                       (qualified-keyword? arg) :ns
                       (string? arg) :message
                       :else :meta) arg))
          {}
          vargs))


(defn wrap-decode-vargs [data]
  "Middleware for vargs"
  (merge data (decode-vargs (-> data
                              :vargs))))


(defn console-appender []
  {:enabled? true
   :async? false
   :min-level nil
   :rate-limit nil
   :output-fn nil
   :fn (fn [data]
         (print (console-logline data)))})


(defn file-appender
  [{:keys [path] :as options}]
  (let [f (File. path)
        nl "\n"]
    {:enabled? true
     :async? false
     :min-level nil
     :rate-limit nil
     :output-fn nil
     :fn (fn [data]
           (spit path (str (logline data) nl) :append (.exists f)))}))


(defn start [{:keys [:level :console? :file-path]}]
  (timbre/merge-config!
    {:level (keyword level)
     :middleware [wrap-decode-vargs]
     :appenders {:console (when console?
                            (console-appender))
                 :file (when file-path
                         (file-appender {:path file-path}))}}))
