define(['jquery',
        'underscore',
        'backbone',
        'models/user'],

      function($, _, Backbone, User){
        
        var GigyaServiceModel = Backbone.Model.extend({
          defaults: {
            'status' : 'unknown',
            'user'   : null
          },

          onLoginHandler: function(eventObj){
            console.log(eventObj);
            var user  = this.context;
            user.login(eventObj.user);
          },
          
          logout: function(){
            gigya.services.socialize.logout();
          },
          
          onLogoutHandler: function(eventObj){
            var user  = this.context;
            user.logout();
          },
          
        });
        
        return GigyaServiceModel;
      });
