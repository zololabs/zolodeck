define(['jquery',
        'underscore',
        'backbone',
        'models/stats',
        'utils/custom_backbone',
        'utils/cookie_utils'],

      function($, _, Backbone, Stats, CustomBackbone, CookieUtils){
        
        var UserModel = Backbone.Model.extend({
          
          urlRoot: "https://zolodev.com:4430/users",

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

          provider: function(){
            return this.get("provider");
          },

          login: function(provider, providerLoginInfo){
            console.log("Logged In : " , provider);
            this.set({'provider':provider, 
                      'providerLoginInfo': providerLoginInfo});
            this.save({},
                      {wait: true, 
                       success: function(user, response) {
                         CookieUtils.setAuthCookie(response.guid);
                         user.set({'state':'LOGGED_IN',
                                   'guid' : response.UID});
                         _kmq.push(['identify', response.guid]);
                         user.stats().fetch();
                       },
                       error: function(user, response){
                         console.log("Error Happened");
                         console.log(response);
                       }
                      }); 
          },

          logout: function(){
            console.log("Logged Out");
            this.set({'service':null, 
                      'state':'LOGGED_OUT',
                      'stats' : new Stats()});
          },
          

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
          }

        });
        
        return UserModel;
      });
