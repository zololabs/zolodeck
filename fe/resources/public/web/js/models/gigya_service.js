define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
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
          
          onLogoutHandler: function(eventObj){
            console.log("On Logout Handler")
            var user  = this.context;
            user.logout();
          },

          onAddConnectionsHandler: function(eventObj){
            console.log("ON Add Connections Handler");
            console.log(eventObj);
            var user = this.context;
            user.save(eventObj);
          }
          
        });
        
        return GigyaServiceModel;
      });
