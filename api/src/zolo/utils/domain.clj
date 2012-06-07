(ns zolo.utils.domain
  (:require [zolodeck.utils.maps :as maps]
            [zolodeck.utils.calendar :as calendar]
            [zolodeck.demonic.schema :as schema]))

(def FB-USER-KEYS 
     {:first_name :user/first-name
      :last_name :user/last-name
      :gender :user/gender
      :link :user/fb-link
      :username :user/fb-username
      :email :user/fb-email
      :id :user/fb-id
      :auth-token :user/fb-auth-token})


(defn force-schema-attrib [attrib value]
  (cond
   (and (schema/is-string? attrib) (not (string? value))) (str value)
   :else value))

(defn force-schema-types [a-map]
  (maps/transform-vals-with a-map force-schema-attrib))

(defn group-by-attrib [objects attrib]
  (-> (group-by attrib objects)
      (maps/transform-vals-with (fn [_ v] (first v)))))

(defn fb-user->user [fb-user]
  (maps/update-all-map-keys fb-user FB-USER-KEYS))
