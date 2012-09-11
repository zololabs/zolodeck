define(['jquery',
        'underscore',
        'backbone',
        'models/stats',
        'utils/custom_backbone',
        'utils/gigya_utils'],

      function($, _, Backbone, Stats, CustomBackbone, GigyaUtils){
        
        var UserModel = Backbone.Model.extend({
          
          urlRoot: "http://localhost:4000/users",

          sync: CustomBackbone.zoloSync,

          idAttribute: "guid",

          defaults: {
            'state': 'LOGGED_OUT',
            'stats': new Stats()
          },

          stats: function() {
            return this.get('stats');
          },

          signup: function(){
            this.save({},
                      {wait: true, 
                       success: function(user, response) {
                         GigyaUtils.setAuthCookie(response.guid);
                         this.set({'state':'LOGGED_IN'});
                         this.stats().fetch();
                       },
                       error: function(user, response){
                         console.log("Error Happened");
                         console.log(response);
                       }
                      }); 
          },
          
          login: function(gigyaUser){
            this.set(gigyaUser);
            if(this.isNewUser()){
              console.log("New User");
              this.signup();
            }else{
              console.log("Returning User");
              console.log(gigyaUser);
              this.set({'guid' : gigyaUser.UID});
              GigyaUtils.setAuthCookie(gigyaUser.UID);
              this.set({'state':'LOGGED_IN'});
              this.stats().fetch();
            }
          },

          logout: function(){
            console.log("Logged Out");
            this.set({'stats': new Stats(), 
                      'state':'LOGGED_OUT'});
            GigyaUtils.cleanupUserCookies();
          },
          
          isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
          },
          
          isLoggedOut: function(){
            return !this.isLoggedIn();
          },

          isNewUser: function(){
            var siteUID = this.get('isSiteUID');
            return ((siteUID == null) || (siteUID == false));
          }
          
        });
        
        return UserModel;
      });
