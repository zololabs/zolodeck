define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){

        return {
          
          ZOLO_GUID : "zolo_guid",

          setPermanentCookieIfNeeded: function(k, v) {
            if ($.cookie(k)) {
              console.log(k + " cookie already set to: " + $.cookie(k));
              return undefined;
            };
            var ten_years = 10 * 365;
            $.cookie(k, v, {path: "/", expires: ten_years});
          },
          
          setAuthCookie: function(guid) {
            console.log('In setUserCookies , Guid :' + guid); 
            this.setPermanentCookieIfNeeded(this.ZOLO_GUID, guid);
          },
          
          cleanupUserCookies: function(){
            console.log("Cleaning up cookies");
            $.removeCookie(this.ZOLO_GUID);
          }
        };

      });
