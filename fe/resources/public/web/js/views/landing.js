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
            _.bindAll(this, 'render', 'renderFacebookLogin', 'loginUsingFacebook');
            
            this.user = this.model;
          },

          render: function(){
            
            var data = {};
            var compiledTemplate = _.template( landingTemplate, data );

            this.$el.html(compiledTemplate);

            this.renderFacebookLogin();

            return this;
          },

          renderFacebookLogin: function(){
            console.log("Rendering Facebook Login");

            var user = this.user;

            window.fbAsyncInit = function() {
              FB.init({
                appId      : '361942873847116',
                //channelUrl : '//WWW.YOUR_DOMAIN.COM/channel.html', // Channel File
                status     : true, // check login status
                cookie     : true, // enable cookies to allow the server to access the session
                xfbml      : true  // parse XFBML
              });
               
              FB.Event.subscribe('auth.statusChange', function(response) {
                if (response && ("connected" == response.status)){
                  user.login("FACEBOOK");
                } else {
                  user.logout();
                }
              });
            };
            
            // Load the SDK Asynchronously
            (function(d){
              var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
              if (d.getElementById(id)) {return;}
              js = d.createElement('script'); js.id = id; js.async = true;
              js.src = "//connect.facebook.net/en_US/all.js";
              ref.parentNode.insertBefore(js, ref);
            }(document));
            
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
          }

        });

        return LandingView;
      });
