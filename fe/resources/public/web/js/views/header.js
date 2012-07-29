define(['jquery',
        'underscore',
        'backbone',
        'text!templates/header.html'],

      function($, _, Backbone, headerTemplate){
        
        var HeaderView = Backbone.View.extend({
          
          el: $(".header"),

          events: {
            'click #logout' : 'logoutUsingFacebook'
          },

          initialize:function () {
            _.bindAll(this, 'render', 'logoutUsingFacebook');
            
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

          logoutUsingFacebook: function(){
            console.log("Logout Pressed");
            FB.logout(function(response) {           
              console.log("Logged Out successfully");
            });
          }
        });

        return HeaderView;
      });
