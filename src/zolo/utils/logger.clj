(ns zolo.utils.logger
  (:require [clojure.tools.logging :as logger]
            [zolo.setup.config :as config])
  (:import [org.slf4j MDC]))

(defmacro trace
  "Trace level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/trace ~@args))

(defmacro debug
  "Debug level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/debug ~@args))


(defmacro info
  "Info level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/info ~@args))

(defmacro warn
  "Warn level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/warn ~@args))

(defmacro error
  "Error level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/error ~@args))

(defmacro fatal
  "Fatal level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/fatal ~@args))

(defmacro with-logging-context [x & body]
  `(let [x# ~x
         ctx# (into {} (. ~MDC getCopyOfContextMap))]
     (try
       (if (map? x#)
         (doall (map (fn [[k# v#]] (. ~MDC put (name k#) (name (or v# "")))) x#)))
       ~@body
       (finally
        (if (map? x#)
          (doall (map (fn [[k# v#]]
                        (. ~MDC remove (name k#))
                        (when-let [old# (get ctx# (name k#))]
                          (. ~MDC put (name k#) old#))) x#)))))))


(defn context [request]
  (merge
   (select-keys request [:request-method :query-string :uri :server-name])
   ;;TODO Create trace-id 
   {:trace-id (str (rand 1000000))
    :environment (config/environment)
    :ip-address (:remote-addr request)}))