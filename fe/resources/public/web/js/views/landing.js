define(['jquery',
        'underscore',
        'backbone',
        'text!templates/landing.html'],

      function($, _, Backbone, landingTemplate){
        
        var LandingView = Backbone.View.extend({
          
          el: $("#content"),

          events: {
            'click #facebook_login': 'loginUsingFacebook'
          },

          initialize:function () {
            _.bindAll(this, 'render', 'loginUsingFacebook');
            
            this.user = this.model;
          },

          render: function(){
            var data = {};
            var compiledTemplate = _.template( landingTemplate, data );

            this.$el.html(compiledTemplate);

            return this;
          },

          loginUsingFacebook: function(){
            var that = this;
            FB.login(function(response){
              if (response.authResponse){
                // Not needed as we listen to Facebook AuthChange Event
                // that.model.login("FACEBOOK"); 
                console.log("Logged in successfully");
              }else{
                console.log(response);
              }
            },{scope : 'email,friends_about_me,friends_birthday,friends_relationship_details,friends_location,friends_likes,friends_website,read_mailbox,offline_access'});
          }

        });

        return LandingView;
      });
