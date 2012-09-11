define(['jquery',
        'underscore',
        'backbone',
        'views/contacts_stats',
        'views/network_stats',
        'text!templates/home.html'],

      function($, _, Backbone, ContactsStatsView, NetworkStatsView, homeTemplate){
        
        var HomeView = Backbone.View.extend({
          
          el: $("#content"),

          initialize:function () {
            _.bindAll(this, 'render');
            
            this.user = this.model;

            var contactsStatsView = new ContactsStatsView({model: this.user.stats()});
            var networkStatsView = new NetworkStatsView({model: this.user.stats()});
          },

          render: function(){
            console.log("Ok Starting to render Home");
            var data = {};
            var compiledTemplate = _.template( homeTemplate, data );

            this.$el.html(compiledTemplate);
            
            return this;
          }

        });

        return HomeView;
      });
