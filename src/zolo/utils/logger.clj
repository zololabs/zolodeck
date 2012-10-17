(ns zolo.utils.logger
  (:require [clojure.tools.logging :as logger]
            [zolodeck.utils.string :as string-utils])
  (:use zolodeck.utils.debug
        clojure.pprint)
  (:import [org.slf4j MDC]))

(defn snip [s]
  (-> s
      (.substring 1 (dec (dec (.length s))))
      (str "\n")))

(defn snipped-pretty [& things]
  (let [s (with-out-str (pprint things))]
    (snip s)))

(defmacro trace
  "Trace level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/trace (snipped-pretty ~@args)))

(defmacro debug
  "Debug level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/debug (snipped-pretty ~@args)))


(defmacro info
  "Info level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/info ~@args))

(defmacro warn
  "Warn level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logger/warn (snipped-pretty ~@args)))

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
         (doall (map (fn [[k# v#]] (. ~MDC put (name k#) (string-utils/to-string v#))) x#)))
       ~@body
       (finally
        (if (map? x#)
          (doall (map (fn [[k# v#]]
                        (. ~MDC remove (name k#))
                        (when-let [old# (get ctx# (name k#))]
                          (. ~MDC put (name k#) old#))) x#)))))))


