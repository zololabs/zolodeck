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
          
          setUserCookies: function(user_details) {
            console.log('In setUserCookies , User Details :');
            console.log(user_details);
            this.setPermanentCookieIfNeeded(ZOLO_GUID, user_details.guid);
            this.setPermanentCookieIfNeeded(ZOLO_UID, user_details.UID);
          },
          
          cleanupUserCookies: function(){
            console.log("Cleaning up cookies");
            $.removeCookie(ZOLO_UID);
            $.removeCookie(ZOLO_GUID);
          }
        };

      });
