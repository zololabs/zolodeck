(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema
        [datomic.api :only [tempid] :as db]))

(def SCHEMA-TX (atom []))

(defn schema-set [schema-set-name & facts]
  (swap! SCHEMA-TX concat facts))

(schema-set "USER ENTITY FACTS"
 (uuid-fact-schema :user/guid false "A GUID for the user" nil false)
 (string-fact-schema :user/first-name true "A user's first name" nil false) 
 (string-fact-schema :user/last-name true "A user's last name" nil false)
 (string-fact-schema :user/login-provider-uid true "A user's login provider uid" nil false)
 (instant-fact-schema :user/last-updated false "The most recent time for when the user was updated" nil false)
 (instant-fact-schema :user/refresh-started false "The most recent time for when the user update was attempted" nil false)
 ;;TODO Add login-provider info

 ;Social details
 (refs-fact-schema :user/user-identities false "A user's social detail records" nil false)   

 ;Contacts Information
 (refs-fact-schema :user/contacts false "A user's contacts" nil false)

 ;Messages Information
 (refs-fact-schema :user/messages false "A contact's messages" nil false)
 (refs-fact-schema :user/temp-messages false "A contact's temp messages" nil false)

  ;Suggested Set
 (string-fact-schema :user/suggestion-set-name false "Set of contact guids that represent a recent suggested set" nil false)
 (refs-fact-schema :user/suggestion-set-contacts false "Set of contact guids that represent a recent suggested set" nil false))

(schema-set "USER IDENTITY FACTS"
 (uuid-fact-schema   :identity/guid false "A GUID for the user identity record" nil false)
 (string-fact-schema :identity/provider-uid  false  "A user's provider UID" nil false)
 (enum-fact-schema   :identity/gender        false  "A user's gender" nil false)
 (string-fact-schema :identity/country       false "A user's age" nil false)
 (string-fact-schema :identity/first-name    false  "A user's first name" nil false)
 (string-fact-schema :identity/last-name     false  "A user's last name" nil false)
 (string-fact-schema :identity/email         false  "A user's email" nil false)
 (long-fact-schema   :identity/birth-day     false  "A user's birthday" nil false)
 (long-fact-schema   :identity/birth-month   false  "A user's birth month" nil false)
 (long-fact-schema   :identity/birth-year    false  "A user's birth year" nil false)
 (string-fact-schema :identity/photo-url     false  "A user's photo url" nil false)
 (string-fact-schema :identity/thumbnail-url false "A user's thumbnail url" nil false)
 (string-fact-schema :identity/profile-url   false  "A user's profile url" nil false)
 (enum-fact-schema   :identity/provider         false  "A user's provider" nil false)
 (string-fact-schema :identity/auth-token    false "The provider specific auth token" nil false)
 (string-fact-schema :identity/state         false  "A user's state" nil false)
 (string-fact-schema :identity/city          false  "A user's city" nil false)
 (string-fact-schema :identity/zip           false  "A user's zip" nil false)
 (string-fact-schema :identity/nickname      false  "A user's nick name" nil false)
 (boolean-fact-schema :identity/permissions-granted false "Whether a user has given permission needed" nil false))

(schema-set "SOCIAL ENTITY FACTS"
 (uuid-fact-schema   :social/guid          false "A GUID for the social details record" nil false)
 (string-fact-schema :social/provider-uid  false  "A user's provider UID" nil false)
 (enum-fact-schema   :social/gender        false  "A user's gender" nil false)
 (string-fact-schema :social/country       false "A user's age" nil false)
 (string-fact-schema :social/first-name    false  "A user's first name" nil false)
 (string-fact-schema :social/last-name     false  "A user's last name" nil false)
 (string-fact-schema :social/email         false  "A user's email" nil false)
 (long-fact-schema   :social/birth-day     false  "A user's birthday" nil false)
 (long-fact-schema   :social/birth-month   false  "A user's birth month" nil false)
 (long-fact-schema   :social/birth-year    false  "A user's birth year" nil false)
 (string-fact-schema :social/photo-url     false  "A user's photo url" nil false)
 (string-fact-schema :social/thumbnail-url false "A user's thumbnail url" nil false)
 (string-fact-schema :social/profile-url   false  "A user's profile url" nil false)
 (enum-fact-schema   :social/provider         false  "A user's provider" nil false)
 (string-fact-schema :social/auth-token    false "The provider specific auth token" nil false)
 (string-fact-schema :social/state         false  "A user's state" nil false)
 (string-fact-schema :social/city          false  "A user's city" nil false)
 (string-fact-schema :social/zip           false  "A user's zip" nil false)
 (string-fact-schema :social/nickname      false  "A user's nick name" nil false)
 )

(schema-set "ENUMS FACTS"
  (enum-value-schema :gender/male)
  (enum-value-schema :gender/female)
  
  (enum-value-schema :provider/facebook)
  (enum-value-schema :provider/linkedin)
  (enum-value-schema :provider/twitter))


(schema-set "CONTACT ENTITY FACTS"
 (uuid-fact-schema   :contact/guid false "A GUID for a contact" nil false)
 ;;TODO Need to decide whether we need these or not           
 (string-fact-schema :contact/first-name true "A contact's first name" nil false) 
 (string-fact-schema :contact/last-name true "A contact's last name" nil false)

 ;Social details
 (refs-fact-schema :contact/social-identities false "A contact's social detail records" nil false)

 (boolean-fact-schema :contact/muted false "Whether a contact is muted or not" nil false)
 (long-fact-schema :contact/score false "A contact's score" nil false))

(schema-set "MESSAGE ENTITY FACTS"
 (uuid-fact-schema    :message/guid false "A GUID for messages" nil false)
 (string-fact-schema  :message/message-id false "ID for this message" nil false)
 ;;TODO Need to store this
 (enum-fact-schema    :message/provider false "The platform: Facebook, LinkedIn, etc" nil false)
 ;;TODO Need to change this to enum
 (string-fact-schema  :message/mode false "Sub-type: wall-post, inbox-message, etc" nil false)
 (strings-fact-schema :message/attachments false "list of links" nil false)
 (string-fact-schema  :message/text true "The body of the message" nil false)
 (instant-fact-schema :message/date false "The date the message was received" nil false)
 (string-fact-schema  :message/from false "The platform ID of the sender" nil false)
 (strings-fact-schema :message/to false "The platform ID of the receiver" nil false)
 (string-fact-schema  :message/thread-id false "The message thread id" nil false)
 (string-fact-schema  :message/reply-to false "The platform ID of the sender" nil false)
 (string-fact-schema  :message/story false "what this message is about" nil false)
 (string-fact-schema  :message/icon false "an icon to represent this message" nil false)
 (string-fact-schema  :message/picture false "a picture about this message" nil false)
 (string-fact-schema  :message/link false "a link about this message" nil false))

(schema-set "TEMP MESSAGE ENTITY FACTS"
 (uuid-fact-schema   :temp-message/guid false "A GUID for temporary messages" nil false)
 (enum-fact-schema   :temp-message/provider false "The provider platform of this temp message" nil false)
 (string-fact-schema :temp-message/mode false "The sub-type of this message" nil false)
 (string-fact-schema :temp-message/text true "The body of this message" nil false)
 (instant-fact-schema :temp-message/date false "The date this message was received/sent" nil false)
 (string-fact-schema :temp-message/from false "The platform ID of the sender" nil false)
 (strings-fact-schema :temp-message/to false "The platform IDs of the receivers" nil false)
 (strings-fact-schema :temp-message/thread-id false "The Thread ID of this message" nil false))

(schema-set "SERVER STATS"
 (uuid-fact-schema :server/guid false "A GUID for the server stats object" nil false)
 (instant-fact-schema :server/last-updated false "When the server last wrote something to datomic" nil true))