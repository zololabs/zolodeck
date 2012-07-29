define(['jquery',
        'underscore',
        'backbone',
        'models/stats'],

      function($, _, Backbone, Stats){
        
        var UserModel = Backbone.Model.extend({
          
          defaults: {
            'service': null,  //Facebook , LinkedIn, Gmail etc
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
                      'state':'LOGGED_OUT'});
          },
          
          service: function(){
            return this.get('service');
          },
          
          isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
          },
          
          isLoggedOut: function(){
            return !this.isLoggedIn();
          }

        });
        
        return UserModel;
      });
