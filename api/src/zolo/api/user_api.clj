(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as gateway]
            [zolo.gigya.core :as gigya-core]
            [zolo.utils.gigya :as gigya]
            [sandbar.auth :as sandbar]
            [zolo.domain.user :as user]
            [zolo.domain.zolo-graph :as zg]
            [zolo.viz.d3 :as d3]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user request-params]
  (-> {:guid (str (:user/guid user))}
      (gigya/add-gigya-uid-info request-params)))

(defn signup-user [request-params]
  (-> request-params
      user/signup-new-user
      (gigya-core/notify-registration request-params)
      (format-user request-params)))

(defn upsert-user [request-params]
  {:user "OK done!"})

(defn friends-list [request-params]
  (gateway/friends-list (:user/fb-auth-token (sandbar/current-user))))

;;TODO Junk function. Need to design the app
(defn fully-loaded-user
  ([user]
     (let [u-fb-id (:user/fb-id user)]
        (if (= 0 (count (:user/contacts user)))
         (do
           (user/update-facebook-friends u-fb-id)
           (user/update-facebook-inbox u-fb-id)
           (user/update-scores (user/reload user))
           (user/reload user))
         user)
         ))
  ([]
     (fully-loaded-user (sandbar/current-user))))

(defn stats [request-params]
  (let [u (fully-loaded-user)
        zg (zg/user->zolo-graph u)]
    {:contacts (zg/contacts-stats zg)
     :network (zg/network-stats zg)}))

(defmulti contact-strengths :client)

;;TODO Dummy functions which returns JSON that D3 lib needs
(defn- node [no]
  {"name" (str "Friend-" no)
   "group" (rand-int 10)})

(defn- link [no]
  {"source" 0
   "target" no
   "value" (rand-int 200)})

(defmethod contact-strengths "d3" [request-params]
  ;;TODO This is an hack for now. We need to come up with design and
  ;;flow of the app
  (let [u (fully-loaded-user)
        zg (zg/user->zolo-graph u)]
    (d3/format-for-d3 zg))

  ;; (let [no-of-nodes 200]
  ;;   {"nodes" (reduce (fn [v no] (conj v (node no))) [{"name" "ME" "group" 1000 "center" true}] (range 1 (+ no-of-nodes 1)))
  ;;    "links" (reduce (fn [v no] (conj v (link no))) [] (range 1 (+ no-of-nodes 1)))})

  )

