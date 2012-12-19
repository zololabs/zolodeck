(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema
        [datomic.api :only [tempid] :as db]))

(def SCHEMA-TX (atom []))

(defn schema-set [schema-set-name & facts]
  (swap! SCHEMA-TX concat facts))

(schema-set "USER ENTITY FACTS"
 (uuid-fact-schema :user/guid false "A GUID for the user")
 (string-fact-schema :user/first-name true "A user's first name") 
 (string-fact-schema :user/last-name true "A user's last name")
 (string-fact-schema :user/login-provider-uid true "A user's login provider uid")
 (instant-fact-schema :user/last-updated false "The most recent time for when the user was updated")
 (instant-fact-schema :user/refresh-started false "The most recent time for when the user update was attempted")
 ;;TODO Add login-provider info

 ;Social details
 (refs-fact-schema :user/user-identities false "A user's social detail records")   

 ;Contacts Information
 (refs-fact-schema :user/contacts false "A user's contacts")

 ;Messages Information
 (refs-fact-schema :user/messages false "A contact's messages")
 (refs-fact-schema :user/temp-messages false "A contact's temp messages"))

(schema-set "USER IDENTITY FACTS"
 (uuid-fact-schema   :identity/guid false "A GUID for the user identity record")
 (string-fact-schema :identity/provider-uid  false  "A user's provider UID")
 (enum-fact-schema   :identity/gender        false  "A user's gender")
 (string-fact-schema :identity/country       false "A user's age")
 (string-fact-schema :identity/first-name    false  "A user's first name")
 (string-fact-schema :identity/last-name     false  "A user's last name")
 (string-fact-schema :identity/email         false  "A user's email")
 (long-fact-schema   :identity/birth-day     false  "A user's birthday")
 (long-fact-schema   :identity/birth-month   false  "A user's birth month")
 (long-fact-schema   :identity/birth-year    false  "A user's birth year")
 (string-fact-schema :identity/photo-url     false  "A user's photo url")
 (string-fact-schema :identity/thumbnail-url false "A user's thumbnail url")
 (string-fact-schema :identity/profile-url   false  "A user's profile url")
 (enum-fact-schema   :identity/provider         false  "A user's provider")
 (string-fact-schema :identity/auth-token    false "The provider specific auth token")
 (string-fact-schema :identity/state         false  "A user's state")
 (string-fact-schema :identity/city          false  "A user's city")
 (string-fact-schema :identity/zip           false  "A user's zip")
 (string-fact-schema :identity/nickname      false  "A user's nick name"))

(schema-set "SOCIAL ENTITY FACTS"
 (uuid-fact-schema   :social/guid          false "A GUID for the social details record")
 (string-fact-schema :social/provider-uid  false  "A user's provider UID")
 (enum-fact-schema   :social/gender        false  "A user's gender")
 (string-fact-schema :social/country       false "A user's age")
 (string-fact-schema :social/first-name    false  "A user's first name")
 (string-fact-schema :social/last-name     false  "A user's last name")
 (string-fact-schema :social/email         false  "A user's email")
 (long-fact-schema   :social/birth-day     false  "A user's birthday")
 (long-fact-schema   :social/birth-month   false  "A user's birth month")
 (long-fact-schema   :social/birth-year    false  "A user's birth year")
 (string-fact-schema :social/photo-url     false  "A user's photo url")
 (string-fact-schema :social/thumbnail-url false "A user's thumbnail url")
 (string-fact-schema :social/profile-url   false  "A user's profile url")
 (enum-fact-schema   :social/provider         false  "A user's provider")
 (string-fact-schema :social/auth-token    false "The provider specific auth token")
 (string-fact-schema :social/state         false  "A user's state")
 (string-fact-schema :social/city          false  "A user's city")
 (string-fact-schema :social/zip           false  "A user's zip")
 (string-fact-schema :social/nickname      false  "A user's nick name")
 )

(schema-set "ENUMS FACTS"
  (enum-value-schema :gender/male)
  (enum-value-schema :gender/female)
  
  (enum-value-schema :provider/facebook)
  (enum-value-schema :provider/linkedin)
  (enum-value-schema :provider/twitter))


(schema-set "CONTACT ENTITY FACTS"
 (uuid-fact-schema   :contact/guid false "A GUID for a contact")
 ;;TODO Need to decide whether we need these or not           
 (string-fact-schema :contact/first-name true "A contact's first name") 
 (string-fact-schema :contact/last-name true "A contact's last name")

 ;Social details
 (refs-fact-schema :contact/social-identities false "A contact's social detail records")

 ;Suggested Date
 (string-fact-schema :contact/suggestion-set false "Set when a contact was suggested")
 
 (long-fact-schema :contact/score false "A contact's score"))

(schema-set "MESSAGE ENTITY FACTS"
 (uuid-fact-schema    :message/guid false "A GUID for messages")
 (string-fact-schema  :message/message-id false "ID for this message")
 ;;TODO Need to store this
 (enum-fact-schema    :message/provider false "The platform: Facebook, LinkedIn, etc")
 ;;TODO Need to change this to enum
 (string-fact-schema  :message/mode false "Sub-type: wall-post, inbox-message, etc")
 (strings-fact-schema :message/attachments false "list of links")
 (string-fact-schema  :message/text true "The body of the message")
 (instant-fact-schema :message/date false "The date the message was received")
 (string-fact-schema  :message/from false "The platform ID of the sender")
 (strings-fact-schema :message/to false "The platform ID of the receiver")
 (string-fact-schema  :message/thread-id false "The message thread id")
 (string-fact-schema  :message/reply-to false "The platform ID of the sender")
 (string-fact-schema  :message/story false "what this message is about")
 (string-fact-schema  :message/icon false "an icon to represent this message")
 (string-fact-schema  :message/picture false "a picture about this message")
 (string-fact-schema  :message/link false "a link about this message"))

(schema-set "TEMP MESSAGE ENTITY FACTS"
 (uuid-fact-schema   :temp-message/guid false "A GUID for temporary messages")
 (enum-fact-schema   :temp-message/provider false "The provider platform of this temp message")
 (string-fact-schema :temp-message/mode false "The sub-type of this message")
 (string-fact-schema :temp-message/text true "The body of this message")
 (instant-fact-schema :temp-message/date false "The date this message was received/sent")
 (string-fact-schema :temp-message/from false "The platform ID of the sender")
 (strings-fact-schema :temp-message/to false "The platform IDs of the receivers")
 (strings-fact-schema :temp-message/thread-id false "The Thread ID of this message"))