define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
        var ZOLO_GUID = "zolo_guid";
        var ZOLO_UID = "zolo_uid";

        return {

            setPermanentCookieIfNeeded: function(k, v) {
                if ($.cookie(k)) {
                    console.log(k + " cookie already set to: " + $.cookie(k));
                    return undefined;
                };
                var ten_years = 10 * 365;
                $.cookie(k, v, {path: "/", expires: ten_years});
            },
          
            notifyRegistration: function(user){
                console.log("Ok notifying ");
                console.log(user);
            },

            setUserCookies: function(user_details) {
                console.log('In setUserCookies');
                console.log('user_details: ' + user_details);
                this.setPermanentCookieIfNeeded(ZOLO_GUID, user_details.guid);
                this.setPermanentCookieIfNeeded(ZOLO_UID, user_details.UID);
            }
        };

      });
