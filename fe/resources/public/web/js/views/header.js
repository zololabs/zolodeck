define(['jquery',
        'underscore',
        'backbone',
        'text!templates/header.html'],

      function($, _, Backbone, headerTemplate){
        
        var HeaderView = Backbone.View.extend({
          
          el: $(".header"),

          events: {
            'click #logout' : 'logoutUsingProvider',
          },

          initialize:function () {
            _.bindAll(this, 'render', 'logoutUsingProvider');
            
            this.user = this.model;
            this.user.bind('change:state', this.render)
          },

          render: function(){
            var data = {};
            var compiledTemplate = _.template( headerTemplate, data );

            this.$el.html(compiledTemplate);
            this.$el.find(".after-login").toggle(this.user.isLoggedIn());

            return this;
          },

          logoutUsingProvider: function(){
            var user = this.user;
            console.log("Logout Pressed");
            if("FACEBOOK" == this.user.provider()){
              FB.logout(function(response) {           
                console.log("Logged Out successfully");
              });
            }
            else if ("LINKEDIN" == this.user.provider()){
              IN.User.logout(function(reponse){
                console.log("Logged out of linked in");
                user.logout();
              });
            }
            else {
              console.log("No Provider Present to logout");
              user.logout();
            }
          }

        });

        return HeaderView;
      });
