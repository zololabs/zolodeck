(ns zolo.api.server-api
  (:use zolodeck.utils.debug)
  (:require
   [zolo.domain.user :as user]
   [zolo.utils.logger :as logger]))

(defn status [request-params]
  {:no-of-users (user/count-users)})



