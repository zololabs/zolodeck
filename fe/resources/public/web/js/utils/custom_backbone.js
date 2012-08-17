define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
        return {
          
          zoloSync : function(method, model, options) {

            console.log("Doing : " + method);
        
            options.beforeSend = function(jqXHR) {
            
              jqXHR.setRequestHeader("Accept", "application/vnd.zololabs.zolodeck.v1+json");
              jqXHR.setRequestHeader("Access-Control-Allow-Origin", "*");
              
            }
        
            // Call the default Backbone sync implementation
            Backbone.sync.call(this, method, model, options);  
          }

        };

      });
