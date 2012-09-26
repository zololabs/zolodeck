define(['jquery',
        'underscore',
        'backbone',
        'models/gigya_service',
        'views/contacts_stats',
        'views/network_stats',
        'text!templates/home.html'],

      function($, _, Backbone, GigyaService, ContactsStatsView, NetworkStatsView, homeTemplate){
        
        var HomeView = Backbone.View.extend({
          
          el: $("#content"),

          initialize:function () {
            _.bindAll(this, 'render', 'renderGigyaAddConnections');
            
            this.user = this.model;

            this.gigyaService = new GigyaService({'user' : this.user});

            gigya.socialize.addEventHandlers({
              context: this.user,
              onConnectionAdded: this.gigyaService.onAddConnectionsHandler
            });
          },

          render: function(){
            console.log("Ok Starting to render Home");
            var data = {};
            var compiledTemplate = _.template( homeTemplate, data );

            this.$el.html(compiledTemplate);

            var contactsStatsView = new ContactsStatsView({model: this.user.stats()});
            var networkStatsView = new NetworkStatsView({model: this.user.stats()});

            this.renderGigyaAddConnections();
            
            return this;
          },

          renderGigyaAddConnections: function(){
            gigya.socialize.showAddConnectionsUI({
              showTermsLink: 'false' ,
              showEditLink: 'true' ,
              hideGigyaLink:true, // remove 'Gigya' link 
              height: 70 ,
              width: 175 ,
              containerID: 'add-connections-div'
            });
          }

        });

        return HomeView;
      });
