define(['jquery',
        'underscore',
        'backbone',
        'models/gigya_service',
        'text!templates/landing.html'],

      function($, _, Backbone, GigyaService, landingTemplate){
        
        var LandingView = Backbone.View.extend({
          
          el: $("#content"),

          events: {
            'click #facebook_login': 'loginUsingFacebook'
          },

          initialize:function () {
            _.bindAll(this, 'render', 'loginUsingFacebook');
            
            this.user = this.model;
            this.gigyaService = new GigyaService({'user' : this.user});
          },

          render: function(){
            
            var data = {};
            var compiledTemplate = _.template( landingTemplate, data );

            this.$el.html(compiledTemplate);

            this.renderGigyaLogin();

            return this;
          },

          renderGigyaLogin: function(){
            gigya.socialize.showLoginUI({ 
	      height: 85 ,
              width: 360 ,
              showTermsLink:false, // remove 'Terms' link 
              hideGigyaLink:true, // remove 'Gigya' link 
              buttonsStyle: 'fullLogo', // Change the default buttons design  to "Full Logos" design 
              showWhatsThis: true, // Pop-up a hint describing the Login Plugin, when the user rolls over the Gigya link. 
              containerID: 'loginDiv', // The component will embed itself  inside the loginDiv Div 
              cid:''
            });
            
            // register for login event
            gigya.socialize.addEventHandlers({
              context: this.user,
              onLogin: this.gigyaService.onLoginHandler,
              onLogout: this.gigyaService.onLogoutHandler
            });
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
