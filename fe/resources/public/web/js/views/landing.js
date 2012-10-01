define(['jquery',
        'underscore',
        'backbone',
        'views/social/facebook_login',
        'views/social/linkedin_login',
        'text!templates/landing.html'],

      function($, _, Backbone, FacebookLoginView, LinkedinLoginView, landingTemplate){
        
        var LandingView = Backbone.View.extend({
          
          el: $("#content"),

          events: {
            'click #facebook_login': 'loginUsingFacebook',
            'click #linkedin_login': 'loginUsingLinkedin'
          },

          initialize:function () {
            _.bindAll(this, 'render', 'loginUsingFacebook');
            
            this.user = this.model;

            var linkedinLoginView = new LinkedinLoginView({model: this.user});
            linkedinLoginView.render();
            
          },

          render: function(){
            
            var data = {};
            var compiledTemplate = _.template( landingTemplate, data );

            this.$el.html(compiledTemplate);
            
            var facebookLoginView = new FacebookLoginView({model: this.user});
            facebookLoginView.render();
 
            return this;
          },

          loginUsingFacebook: function(){
            //var that = this;
            FB.login(function(response){
              if (response.authResponse){
                // Not needed as we listen to Facebook AuthChange Event
                // that.model.login("FACEBOOK"); 
                console.log("Logged in successfully");
              }else{
                console.log(response);
              }
            },{scope : 'email,friends_about_me,friends_birthday,friends_relationship_details,friends_location,friends_likes,friends_website,read_mailbox,offline_access'});
          },

          loginUsingLinkedin: function(){
            var user = this.user;
            IN.User.authorize(
              function(){
                IN.API.Profile("me").result(
                  function(response) {
                    console.log(response);   
                    user.login("LINKEDIN",response);
                  });
               }
            );
          }

                                               
        });
        return LandingView;
      });
