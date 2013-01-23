(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema
        [datomic.api :only [tempid] :as db]))

(def SCHEMA-TX (atom []))

(defn schema-set [schema-set-name & facts]
  (swap! SCHEMA-TX concat facts))

(schema-set "USER ENTITY FACTS"
 (uuid-fact-schema :user/guid false "A GUID for the user" false)
 (string-fact-schema :user/first-name true "A user's first name" false) 
 (string-fact-schema :user/last-name true "A user's last name" false)
 (string-fact-schema :user/login-provider-uid true "A user's login provider uid" false)
 (instant-fact-schema :user/last-updated false "The most recent time for when the user was updated" false)
 (instant-fact-schema :user/refresh-started false "The most recent time for when the user update was attempted" false)
 ;;TODO Add login-provider info

 ;Social details
 (refs-fact-schema :user/user-identities false "A user's social detail records" false)   

 ;Contacts Information
 (refs-fact-schema :user/contacts false "A user's contacts" false)

 ;Messages Information
 (refs-fact-schema :user/messages false "A contact's messages" false)
 (refs-fact-schema :user/temp-messages false "A contact's temp messages" false)

  ;Suggested Set
 (string-fact-schema :user/suggestion-set-name false "Set of contact guids that represent a recent suggested set" false)
 (refs-fact-schema :user/suggestion-set-contacts false "Set of contact guids that represent a recent suggested set" false))

(schema-set "USER IDENTITY FACTS"
 (uuid-fact-schema   :identity/guid false "A GUID for the user identity record" false)
 (string-fact-schema :identity/provider-uid  false  "A user's provider UID" false)
 (enum-fact-schema   :identity/gender        false  "A user's gender" false)
 (string-fact-schema :identity/country       false "A user's age" false)
 (string-fact-schema :identity/first-name    false  "A user's first name" false)
 (string-fact-schema :identity/last-name     false  "A user's last name" false)
 (string-fact-schema :identity/email         false  "A user's email" false)
 (long-fact-schema   :identity/birth-day     false  "A user's birthday" false)
 (long-fact-schema   :identity/birth-month   false  "A user's birth month" false)
 (long-fact-schema   :identity/birth-year    false  "A user's birth year" false)
 (string-fact-schema :identity/photo-url     false  "A user's photo url" false)
 (string-fact-schema :identity/thumbnail-url false "A user's thumbnail url" false)
 (string-fact-schema :identity/profile-url   false  "A user's profile url" false)
 (string-fact-schema :identity/locale        false  "A user's locale" false)
 (enum-fact-schema   :identity/provider      false  "A user's provider" false)
 (string-fact-schema :identity/auth-token    false "The provider specific auth token" false)
 (string-fact-schema :identity/state         false  "A user's state" false)
 (string-fact-schema :identity/city          false  "A user's city" false)
 (string-fact-schema :identity/zip           false  "A user's zip" false)
 (string-fact-schema :identity/nickname      false  "A user's nick name" false)
 (boolean-fact-schema :identity/permissions-granted false "Whether a user has given permission needed" false))

(schema-set "SOCIAL ENTITY FACTS"
 (uuid-fact-schema   :social/guid          false "A GUID for the social details record" false)
 (string-fact-schema :social/provider-uid  false  "A user's provider UID" false)
 (enum-fact-schema   :social/gender        false  "A user's gender" false)
 (string-fact-schema :social/country       false "A user's age" false)
 (string-fact-schema :social/first-name    false  "A user's first name" false)
 (string-fact-schema :social/last-name     false  "A user's last name" false)
 (string-fact-schema :social/email         false  "A user's email" false)
 (long-fact-schema   :social/birth-day     false  "A user's birthday" false)
 (long-fact-schema   :social/birth-month   false  "A user's birth month" false)
 (long-fact-schema   :social/birth-year    false  "A user's birth year" false)
 (string-fact-schema :social/photo-url     false  "A user's photo url" false)
 (string-fact-schema :social/thumbnail-url false "A user's thumbnail url" false)
 (string-fact-schema :social/profile-url   false  "A user's profile url" false)
 (enum-fact-schema   :social/provider         false  "A user's provider" false)
 (string-fact-schema :social/auth-token    false "The provider specific auth token" false)
 (string-fact-schema :social/state         false  "A user's state" false)
 (string-fact-schema :social/city          false  "A user's city" false)
 (string-fact-schema :social/zip           false  "A user's zip" false)
 (string-fact-schema :social/nickname      false  "A user's nick name" false)
 )

(schema-set "ENUMS FACTS"
  (enum-value-schema :gender/male)
  (enum-value-schema :gender/female)
  
  (enum-value-schema :provider/facebook)
  (enum-value-schema :provider/linkedin)
  (enum-value-schema :provider/twitter))


(schema-set "CONTACT ENTITY FACTS"
 (uuid-fact-schema   :contact/guid false "A GUID for a contact" false)
 ;;TODO Need to decide whether we need these or not           
 (string-fact-schema :contact/first-name true "A contact's first name" false) 
 (string-fact-schema :contact/last-name true "A contact's last name" false)

 ;Social details
 (refs-fact-schema :contact/social-identities false "A contact's social detail records" false)

 (boolean-fact-schema :contact/muted false "Whether a contact is muted or not" false)
 (long-fact-schema :contact/score false "A contact's score" false))

(schema-set "MESSAGE ENTITY FACTS"
 (uuid-fact-schema    :message/guid false "A GUID for messages" false)
 (string-fact-schema  :message/message-id false "ID for this message" false)
 ;;TODO Need to store this
 (enum-fact-schema    :message/provider false "The platform: Facebook, LinkedIn, etc" false)
 ;;TODO Need to change this to enum
 (string-fact-schema  :message/mode false "Sub-type: wall-post, inbox-message, etc" false)
 (strings-fact-schema :message/attachments false "list of links" false)
 (string-fact-schema  :message/text true "The body of the message" false)
 (instant-fact-schema :message/date false "The date the message was received" false)
 (string-fact-schema  :message/from false "The platform ID of the sender" false)
 (strings-fact-schema :message/to false "The platform ID of the receiver" false)
 (string-fact-schema  :message/thread-id false "The message thread id" false)
 (string-fact-schema  :message/reply-to false "The platform ID of the sender" false)
 (string-fact-schema  :message/story false "what this message is about" false)
 (string-fact-schema  :message/icon false "an icon to represent this message" false)
 (string-fact-schema  :message/picture false "a picture about this message" false)
 (string-fact-schema  :message/link false "a link about this message" false))

(schema-set "TEMP MESSAGE ENTITY FACTS"
 (uuid-fact-schema   :temp-message/guid false "A GUID for temporary messages" false)
 (enum-fact-schema   :temp-message/provider false "The provider platform of this temp message" false)
 (string-fact-schema :temp-message/mode false "The sub-type of this message" false)
 (string-fact-schema :temp-message/text true "The body of this message" false)
 (instant-fact-schema :temp-message/date false "The date this message was received/sent" false)
 (string-fact-schema :temp-message/from false "The platform ID of the sender" false)
 (strings-fact-schema :temp-message/to false "The platform IDs of the receivers" false)
 (strings-fact-schema :temp-message/thread-id false "The Thread ID of this message" false))

(schema-set "SERVER STATS"
 (uuid-fact-schema :server/guid false "A GUID for the server stats object" false)
 (instant-fact-schema :server/last-updated false "When the server last wrote something to datomic" true))