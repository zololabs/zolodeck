(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema))

(def USER-SCHEMA-TX 
  [(uuid-fact-schema :user/guid false "A GUID for the user")
   (string-fact-schema :user/first-name true "A user's first name") 
   (string-fact-schema :user/last-name true "A user's last name") 
   (string-fact-schema :user/gender false "A user's gender")

   (refs-fact-schema :user/social-details false "A user's social detail records")   

   ;Contacts Information
   (refs-fact-schema :user/contacts false "A user's contacts")
   ])

(def SOCIAL-DETAILS-TX
  [(uuid-fact-schema :social/guid false "A GUID for the social details record")
   (long-fact-schema :social/age false "A user's age")
   (string-fact-schema :social/country false "A user's age")
   (ref-fact-schema :social/gender      false  "A user's gender")
   (string-fact-schema :social/lastName    false  "A user's last name")
   (string-fact-schema :social/state       false  "A user's state")
   (string-fact-schema :social/photoURL    false  "A user's photo url")
   (long-fact-schema :social/birthDay    false  "A user's birthday")
   (string-fact-schema :social/thumbnailURL false "A user's thumbnail url")
   (string-fact-schema :social/firstName   false  "A user's first name")
   (string-fact-schema :social/city        false  "A user's city")
   (long-fact-schema :social/birthMonth  false  "A user's birth month")
   (string-fact-schema :social/nickname    false  "A user's nick name")
   (long-fact-schema :social/birthYear   false  "A user's birth year")
   (string-fact-schema :social/email       false  "A user's email")
   (string-fact-schema :social/profileURL  false  "A user's profile url")
   (string-fact-schema :social/providerUID false  "A user's provider UID")
   (string-fact-schema :social/zip         false  "A user's zip")

   (ref-fact-schema :social/provider    false  "A user's provider")
   (string-fact-schema :social/auth-token false "The provider specific auth token")
   ])

(def ENUMS-TX
  [(enum-value-schema :gender/male)
   (enum-value-schema :gender/female)

   (enum-value-schema :provider/facebook)
   (enum-value-schema :provider/linkedin)   
   ])


(def CONTACT-SCHEMA-TX 
  [(uuid-fact-schema :contact/guid false "A GUID for a contact")
   (string-fact-schema :contact/first-name true "A contact's first name") 
   (string-fact-schema :contact/last-name true "A contact's last name") 
   (string-fact-schema :contact/gender false "A contact's gender") 
   ;Facebook Information
   (string-fact-schema :contact/fb-id false "A contact's Facebook ID") 
   (string-fact-schema :contact/fb-link false "A contact's Facebook link") 
   (instant-fact-schema :contact/fb-birthday false "A contact's Facebook BirthDay") 
   (string-fact-schema :contact/fb-picture-link false "A contact's Facebook Picture Link")
   ;Messages Information
   (refs-fact-schema :contact/messages false "A contact's messages")
   ;Scores Information
   (refs-fact-schema :contact/scores false "A contact's scores")])

(def MESSAGE-SCHEMA-TX
  [(uuid-fact-schema :message/guid false "A GUID for messages")
   (string-fact-schema :message/message-id false "ID for this message")
   (string-fact-schema :message/platform false "The platform: Facebook, LinkedIn, etc")
   (string-fact-schema :message/mode false "Sub-type: wall-post, inbox-message, etc")
   (strings-fact-schema :message/attachments false "list of links")
   (string-fact-schema :message/text true "The body of the message")
   (instant-fact-schema :message/date false "The date the message was received")
   (string-fact-schema :message/from false "The platform ID of the sender")
   (string-fact-schema :message/to false "The platform ID of the receiver")
   (string-fact-schema :message/thread-id false "The message thread id")
   (string-fact-schema :message/reply-to false "The platform ID of the sender")])

(def SCORE-SCHEMA-TX
  [(uuid-fact-schema :score/guid false "A GUID for score")
   (long-fact-schema :score/value false "Contact Score value")
   (instant-fact-schema :score/at false "The date when score was calculated")])

(def SCHEMA-TX
  (concat USER-SCHEMA-TX
          CONTACT-SCHEMA-TX
          MESSAGE-SCHEMA-TX
          SCORE-SCHEMA-TX))