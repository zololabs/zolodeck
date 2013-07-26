(use 'zolo.demonic.core 'zolo.utils.clojure)


(require '[zolo.domain.user :as u]
         '[zolo.domain.contact :as c]
         '[zolo.domain.message :as m]
         '[zolo.domain.interaction :as int]
         '[zolo.domain.user-identity :as ui]

         '[zolo.store.user-store :as u-store]
         '[zolo.store.message-store :as m-store]
         '[zolo.store.user-identity-store :as ui-store]

         '[zolo.utils.calendar :as zolo-cal]

         '[zolo.api.user-api :as uapi]
         '[datomic.api :as db]
         '[zolo.demonic.core :as demonic]
         '[zolo.demonic.helper :as dh] )


(do (zolo.setup.config/setup-config)
    (zolo.setup.datomic-setup/init-datomic))

(in-demarcation (def siva (nth (u/find-all-users) 1)))


(defn loade [eid] (in-demarcation (db/touch (db/entity @zolo.demonic.helper/DATOMIC-DB eid))))



(defn deets [triples]
        (let [g (group-by last triples)]
          [(:db/txInstant (loade (ffirst triples))) (ffirst triples) (count (g true)) (count (g false))]))
