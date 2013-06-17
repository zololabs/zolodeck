(ns zolo.setup.datomic-schema
  (:use zolo.demonic.schema
        [datomic.api :only [tempid] :as db]))

(def SCHEMA-TX (atom []))

(defn schema-set [schema-set-name & facts]
  (swap! SCHEMA-TX concat facts))

(schema-set "USER ENTITY FACTS"
 (uuid-fact-schema    :user/guid                   "A GUID for the user" :uniqueness :db.unique/identity :index? true)
 (instant-fact-schema :user/data-ready-in          "The most recent time for when the user was updated")
 (long-fact-schema    :user/login-tz               "A user's timezone offset when she logged in")
 (instant-fact-schema :user/last-updated           "The most recent time for when the user was updated")
 (instant-fact-schema :user/refresh-started        "The most recent time for when the user update was attempted")

 ;Social details
 (refs-fact-schema    :user/user-identities        "A user's social detail records" :component? true)   

 ;Contacts Information
 (refs-fact-schema    :user/contacts               "A user's contacts" :component? true)

 ;Messages Information
 (refs-fact-schema    :user/messages                "A contact's messages" :component? true)
 (refs-fact-schema    :user/temp-messages           "A contact's temp messages" :component? true)

 ;Suggested Set
 (refs-fact-schema    :user/suggestion-sets         "Set of suggestion sets" :component? true))

(schema-set "SUGGESTION SET"
 (uuid-fact-schema    :suggestion-set/guid          "A GUID for suggestion set record" :uniqueness :db.unique/identity)
 (string-fact-schema  :suggestion-set/name          "A suggestion set name")
 (refs-fact-schema    :suggestion-set/contacts      "A suggested contacts"))

(schema-set "USER IDENTITY FACTS"
 (uuid-fact-schema   :identity/guid                 "A GUID for the user identity record" :uniqueness :db.unique/identity)
 (string-fact-schema :identity/provider-uid         "A user's provider UID")
 (enum-fact-schema   :identity/gender               "A user's gender")
 (string-fact-schema :identity/country              "A user's age")
 (string-fact-schema :identity/first-name           "A user's first name")
 (string-fact-schema :identity/last-name            "A user's last name")
 (string-fact-schema :identity/email                "A user's email")
 (long-fact-schema   :identity/birth-day            "A user's birthday")
 (long-fact-schema   :identity/birth-month          "A user's birth month")
 (long-fact-schema   :identity/birth-year           "A user's birth year")
 (string-fact-schema :identity/photo-url            "A user's photo url")
 (string-fact-schema :identity/thumbnail-url        "A user's thumbnail url")
 (string-fact-schema :identity/profile-url          "A user's profile url")
 (string-fact-schema :identity/locale               "A user's locale")
 (enum-fact-schema   :identity/provider             "A user's provider")
 (string-fact-schema :identity/auth-token           "The provider specific auth token")
 (string-fact-schema :identity/state                "A user's state")
 (string-fact-schema :identity/city                 "A user's city")
 (string-fact-schema :identity/zip                  "A user's zip")
 (string-fact-schema :identity/nickname             "A user's nick name")
 (boolean-fact-schema :identity/permissions-granted "Whether a user has given permission needed"))

(schema-set "SOCIAL ENTITY FACTS"
 (uuid-fact-schema    :social/guid                  "A GUID for the social details record" :uniqueness :db.unique/identity)
 (string-fact-schema  :social/provider-uid          "A user's provider UID")
 (string-fact-schema  :social/ui-provider-uid       "The UserIdentity this SI is associated with")
 (float-fact-schema   :social/email-person-score    "Person score as computed by Pento")
 (enum-fact-schema    :social/gender                "A user's gender")
 (string-fact-schema  :social/country               "A user's age")
 (string-fact-schema  :social/first-name            "A user's first name")
 (string-fact-schema  :social/last-name             "A user's last name")
 (string-fact-schema  :social/email                 "A user's email")
 (long-fact-schema    :social/birth-day             "A user's birthday")
 (long-fact-schema    :social/birth-month           "A user's birth month")
 (long-fact-schema    :social/birth-year            "A user's birth year")
 (string-fact-schema  :social/photo-url             "A user's photo url")
 (string-fact-schema  :social/thumbnail-url         "A user's thumbnail url")
 (string-fact-schema  :social/profile-url           "A user's profile url")
 (enum-fact-schema    :social/provider              "A user's provider")
 (string-fact-schema  :social/auth-token            "The provider specific auth token")
 (string-fact-schema  :social/state                 "A user's state")
 (string-fact-schema  :social/city                  "A user's city")
 (string-fact-schema  :social/zip                   "A user's zip")
 (string-fact-schema  :social/nickname              "A user's nick name"))

(schema-set "ENUMS FACTS"
  (enum-value-schema :gender/male)
  (enum-value-schema :gender/female)
  
  (enum-value-schema :provider/facebook)
  (enum-value-schema :provider/email)
  (enum-value-schema :provider/linkedin)
  (enum-value-schema :provider/twitter))


(schema-set "CONTACT ENTITY FACTS"
 (uuid-fact-schema    :contact/guid              "A GUID for a contact" :uniqueness :db.unique/identity)
 (refs-fact-schema    :contact/social-identities "A contact's social detail records" :component? true)
 (boolean-fact-schema :contact/muted             "Whether a contact is muted or not")
 (boolean-fact-schema :contact/is-a-person       "Marked true if user marks this as a person, or false if s/he marked as not a person") 
 (long-fact-schema    :contact/score             "A contact's score"))

(schema-set "MESSAGE ENTITY FACTS"
 (uuid-fact-schema    :message/guid              "A GUID for messages" :uniqueness :db.unique/identity)
 (ref-fact-schema     :message/user-identity     "The UI this message was sourced from")
 (string-fact-schema  :message/message-id        "ID for this message" :uniqueness :db.unique/identity)
 ;;TODO Need to store this
 (enum-fact-schema    :message/provider          "The platform: Facebook, LinkedIn, etc")
 ;;TODO Need to change this to enum
 (strings-fact-schema :message/attachments       "list of links")
 (string-fact-schema  :message/subject           "the subject of this message" :index? true)
 (string-fact-schema  :message/text              "The body of the message" :index? true)
 (string-fact-schema  :message/snippet           "The body of the message")
 (instant-fact-schema :message/date              "The date the message was received")
 (string-fact-schema  :message/from              "The platform ID of the sender")
 (strings-fact-schema :message/to                "The platform ID of the receiver")
 (string-fact-schema  :message/thread-id         "The message thread id")
 (string-fact-schema  :message/reply-to          "The platform ID of the sender")
 (string-fact-schema  :message/story             "what this message is about")
 (string-fact-schema  :message/icon              "an icon to represent this message")
 (string-fact-schema  :message/picture           "a picture about this message")
 (string-fact-schema  :message/link              "a link about this message"))

(schema-set "TEMP MESSAGE ENTITY FACTS"
 (uuid-fact-schema    :temp-message/guid         "A GUID for temporary messages" :uniqueness :db.unique/identity)
 (enum-fact-schema    :temp-message/provider     "The provider platform of this temp message")
 (string-fact-schema  :temp-message/subject      "The subject of this temp message" :index? true)
 (string-fact-schema  :temp-message/text         "The body of this message" :index? true)
 (instant-fact-schema :temp-message/date         "The date this message was received/sent")
 (string-fact-schema  :temp-message/from         "The platform ID of the sender")
 (strings-fact-schema :temp-message/to           "The platform IDs of the receivers")
 (string-fact-schema  :temp-message/thread-id    "The Thread ID of this message"))

(schema-set "SERVER STATS"
 (uuid-fact-schema :server/guid                  "A GUID for the server stats object" :uniqueness :db.unique/identity )
 (instant-fact-schema :server/last-updated       "When the server last wrote something to datomic" :no-history? true))