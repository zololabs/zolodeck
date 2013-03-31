(ns zolo.storm.fns.update-messages
  (:use zolodeck.utils.debug
        zolo.storm.utils))

(gen-class
 :name zolo.storm.fns.UpdateMessages
 :extends storm.trident.operation.BaseFunction)

(defn -execute [this tuple collector]
  (let [contact-guid (.getStringByField tuple "contact-guid")]
    (print-vals "updating messages for contact guid:" contact-guid)))