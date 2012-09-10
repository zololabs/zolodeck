define(['jquery',
        'underscore',
        'backbone',
        'utils/gigya_utils'],

      function($, _, Backbone, GigyaUtils){
        
        return {
          
          zoloSync : function(method, model, options) {

            console.log("Doing : " + method);
        
            options.beforeSend = function(jqXHR) {
            
              jqXHR.setRequestHeader("Accept", "application/vnd.zololabs.zolodeck.v1+json");
              jqXHR.setRequestHeader("Access-Control-Allow-Origin", "*");

              var zolo_guid = $.cookie(GigyaUtils.ZOLO_GUID);

              if(zolo_guid){
                jqXHR.setRequestHeader("Authorization", "Bearer " + zolo_guid);
              }
              
            }
        
            // Call the default Backbone sync implementation
            Backbone.sync.call(this, method, model, options);  
          }

        };

      });
