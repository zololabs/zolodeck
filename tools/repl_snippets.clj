(use 'zolodeck.demonic.core 'zolodeck.utils.clojure)


(require '[zolo.domain.user :as u] '[zolo.stats.activity :as act] '[zolo.domain.interaction :as int] '[zolodeck.utils.calendar :as zolo-cal] '[zolo.domain.accessors :as dom] '[zolo.api.user-api :as uapi] '[zolo.domain.user-identity :as ui] '[datomic.api :as db] '[zolodeck.demonic.helper :as dh])


(do (zolo.setup.config/setup-config)
    (zolo.setup.datomic-setup/init-datomic))

(in-demarcation (def siva (nth (u/find-all-users) 18)))