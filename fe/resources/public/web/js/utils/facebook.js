define(['jquery',
        'underscore',
        'backbone',
        'utils/cookies'],

      function($, _, Backbone, Cookies){
        
        return {
          appId      : '361942873847116',

          signed_request : function(){
            return Cookies.read_cookie("fbsr_" + this.appId);
          }
        };

      });
