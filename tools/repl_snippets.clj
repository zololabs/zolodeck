(use 'zolo.demonic.core 'zolo.utils.clojure)


(require '[zolo.domain.user :as u] '[zolo.domain.contact :as c] '[zolo.domain.message :as m] '[zolo.stats.activity :as act] '[zolo.domain.interaction :as int] '[zolo.utils.calendar :as zolo-cal] '[zolo.domain.accessors :as dom] '[zolo.api.user-api :as uapi] '[zolo.domain.user-identity :as ui] '[datomic.api :as db] '[zolo.demonic.helper :as dh])


(do (zolo.setup.config/setup-config)
    (zolo.setup.datomic-setup/init-datomic))

(in-demarcation (def siva (nth (u/find-all-users) 1)))


(defn loade [eid] (in-demarcation (db/touch (db/entity @zolo.demonic.helper/DATOMIC-DB eid))))



(defn deets [triples]
        (let [g (group-by last triples)]
          [(:db/txInstant (loade (ffirst triples))) (ffirst triples) (count (g true)) (count (g false))]))
