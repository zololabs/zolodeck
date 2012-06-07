(ns zolo.domain.message
  (:use zolo.setup.datomic-setup        
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [clojure.set :as set]))

(defn merge-messages [user fresh-messages]
  (let [existing-messages-grouped (group-by-attrib (:user/messages user) :message/message-id)
        fresh-messages-grouped (group-by-attrib fresh-messages :message/message-id)
        new-message-ids  (set/difference (-> fresh-messages-grouped keys set)
                                         (-> existing-messages-grouped keys set))
        added-messages (map fresh-messages-grouped new-message-ids)]
    (assoc user :user/messages added-messages)))

