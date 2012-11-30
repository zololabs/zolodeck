(ns zolo.storm.fns.print-vals
  (:use zolodeck.utils.debug
        zolo.storm.utils))

(gen-class
 :name zolo.storm.fns.PrintVals
 :extends storm.trident.operation.BaseFunction)

(defn -execute [this tuple collector]
  (print-vals "TUPLE:" tuple)
  ;(.emit collector (values (.getStringByField tuple "args")))
  )