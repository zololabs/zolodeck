define(['jquery',
        'underscore',
        'backbone',
        'utils/cookie_utils'],

      function($, _, Backbone, CookieUtils){
        
        return {
          
          zoloSync : function(method, model, options) {

            console.log("Doing : " + method);

            options.xhrFields = {withCredentials: true};

            options.beforeSend = function(jqXHR) {
            
              jqXHR.setRequestHeader("Accept", "application/vnd.zololabs.zolodeck.v1+json");
              jqXHR.setRequestHeader("Access-Control-Allow-Origin", "https://zolodev.com");

              var zolo_guid = $.cookie(CookieUtils.ZOLO_GUID);

              if(zolo_guid){
                jqXHR.setRequestHeader("Authorization", "Bearer " + zolo_guid);
              }
              
            }
        
            // Call the default Backbone sync implementation
            Backbone.sync.call(this, method, model, options);  
          }

        };

      });
