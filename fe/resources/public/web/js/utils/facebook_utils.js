define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
        return {
          appId      : '361942873847116',

          signed_request : function(){
            return $.cookie("fbsr_" + this.appId);
          }
        };

      });
