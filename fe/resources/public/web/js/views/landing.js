define(['jquery',
        'underscore',
        'backbone',
        'models/gigya_service',
        'text!templates/landing.html'],

      function($, _, Backbone, GigyaService, landingTemplate){
        
        var LandingView = Backbone.View.extend({
          
          el: $("#content"),

          initialize:function () {
            _.bindAll(this, 'render', 'renderGigyaLogin');
            
            this.user = this.model;
            this.gigyaService = new GigyaService({'user' : this.user});

            // register for login event
            gigya.socialize.addEventHandlers({
              context: this.user,
              onLogin: this.gigyaService.onLoginHandler,
              onLogout: this.gigyaService.onLogoutHandler
            });
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
              cid:'',
              lastLoginIndication : "star",
              extraPermissions : "r_fullprofile,r_emailaddress,r_network,w_messages,rw_nus",
              buttonsStyle: "standard"
            });
 
          },

        });

        return LandingView;
      });
