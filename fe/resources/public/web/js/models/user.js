define(['jquery',
        'underscore',
        'backbone',
        'models/stats',
        'utils/custom_backbone',
        'utils/gigya_utils'],

      function($, _, Backbone, Stats, CustomBackbone, GigyaUtils){
        
        var UserModel = Backbone.Model.extend({
          
          url: "http://localhost:4000/users",

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
            this.save(this,
                      {error: function(user, response){
                        console.log(response);
                      },
                       success: function(user, response) {
                         GigyaUtils.notifyRegistration(user);
                         GigyaUtils.setUserCookies(response);
                       }
                      }); 
          },
          
          login: function(gigyaUser){
            this.set(gigyaUser);
            this.set({'state':'LOGGED_IN'});
            if(this.isNewUser()){
                this.signup()
            }
          },

          logout: function(){
            console.log("Logged Out");
            this.set({'state':'LOGGED_OUT', 
                      'contactStrengthsD3':  new VisualizerD3()});
          },
          
          logout: function(){
            console.log("Logged Out");
            this.set({'stats':null, 
                      'state':'LOGGED_OUT'});
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
