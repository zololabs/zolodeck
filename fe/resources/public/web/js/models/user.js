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
            'provider' : null, //Facebook, LinkedIn, Gmail
            'state': 'LOGGED_OUT',
            'stats': new Stats()
          },

          stats: function() {
            return this.get('stats');
          },

          login: function(service){
            console.log("Logged In : " , service);
            this.set({'service':service, 'state':'LOGGED_IN'});
            this.stats().fetch();
          },

          logout: function(){
            console.log("Logged Out");
            this.set({'service':null, 
                      'state':'LOGGED_OUT',
                       'stats' : new Stats()});
          },
          

          // signup: function(){
          //   this.save({},
          //             {wait: true, 
          //              success: function(user, response) {
          //                GigyaUtils.setAuthCookie(response.guid);
          //                user.set({'state':'LOGGED_IN'});
          //                user.stats().fetch();
          //              },
          //              error: function(user, response){
          //                console.log("Error Happened");
          //                console.log(response);
          //              }
          //             }); 
          // },
          
          // login: function(gigyaUser){
          //   this.set(gigyaUser);
          //   if(this.isNewUser()){
          //     console.log("New User");
          //     this.signup();
          //   }else{
          //     console.log("Returning User");
          //     console.log(gigyaUser);
          //     this.set({'guid' : gigyaUser.UID});
          //     GigyaUtils.setAuthCookie(gigyaUser.UID);
          //     this.set({'state':'LOGGED_IN'});
          //     this.stats().fetch();
          //   }
          // },

          // logout: function(){
          //   console.log("Logged Out");
          //   this.set({'stats': new Stats(), 
          //             'state':'LOGGED_OUT'});
          //   GigyaUtils.cleanupUserCookies();
          // },
          
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
