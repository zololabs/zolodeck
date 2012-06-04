(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema))

(def USER-SCHEMA-TX 
  [(string-fact-schema :user/first-name true "A user's first name") 
   (string-fact-schema :user/last-name true "A user's last name") 
   (string-fact-schema :user/gender false "A user's gender") 
   ;Facebook Information
   (string-fact-schema :user/fb-id false "A user's Facebook ID") 
   (string-fact-schema :user/fb-auth-token false "A user's Facebook auth token") 
   (string-fact-schema :user/fb-email false "A user's Facebook email") 
   (string-fact-schema :user/fb-link false "A user's Facebook link") 
   (string-fact-schema :user/fb-username false "A user's Facebook username")
   ;Contacts Information
   (refs-fact-schema :user/contacts false "A user's contacts")
   ;Messages Information
   (refs-fact-schema :user/messages false "A user's messages")])


(def CONTACT-SCHEMA-TX 
  [(string-fact-schema :contact/first-name true "A contact's first name") 
   (string-fact-schema :contact/last-name true "A contact's last name") 
   (string-fact-schema :contact/gender false "A contact's gender") 
   ;Facebook Information
   (string-fact-schema :contact/fb-id false "A contact's Facebook ID") 
   (string-fact-schema :contact/fb-link false "A contact's Facebook link") 
   (instant-fact-schema :contact/fb-birthday false "A contact's Facebook BirthDay") 
   (string-fact-schema :contact/fb-picture-link false "A contact's Facebook Picture Link")])

(def MESSAGE-SCHEMA-TX
  [(string-fact-schema :message/message-id false "ID for this message")
   (string-fact-schema :message/platform false "The platform: Facebook, LinkedIn, etc")
   (string-fact-schema :message/mode false "Sub-type: wall-post, inbox-message, etc")
   (strings-fact-schema :message/attachments false "list of links")
   (string-fact-schema :message/text true "The body of the message")
   (instant-fact-schema :message/date false "The date the message was received")
   (string-fact-schema :message/from false "The from ID of the sender")
   (string-fact-schema :message/to false "The from ID of the receiver")
   (string-fact-schema :message/thread-id false "The from ID of the sender")
   (string-fact-schema :message/reply-to false "The from ID of the sender")])

(def SCHEMA-TX
  (concat USER-SCHEMA-TX
          CONTACT-SCHEMA-TX
          MESSAGE-SCHEMA-TX))