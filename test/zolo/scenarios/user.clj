(ns zolo.scenarios.user)

(def default-fb-login-credentials {:provider "FACEBOOK",
                                   :state "LOGGED_IN",
                                   :permissions_granted true,
                                   :stats {:network nil, :other nil, :connectSoonContacts nil},
                                   :client-tz "480"
                                   :providerLoginInfo
                                   {:authResponse
                                    {:userID "100003928558336",
                                     :expiresIn "5855"},
                                    :status "connected"}})