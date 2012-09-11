define(['jquery',
        'underscore',
        'backbone',
        'text!templates/contacts_stats.html'],

      function($, _, Backbone, contactsStatsTemplate){
        
        var ContactsStatsView = Backbone.View.extend({
          
          initialize: function () {
            _.bindAll(this, 'render');
            
            this.stats = this.model;
            this.stats.bind("change", this.render, this);
          },
        
          render: function (eventName) {
            console.log("Rendering Contacts Stats");
            var data = this.stats.contacts();
            var compiledTemplate = _.template( contactsStatsTemplate, data );

            $("#contacts-stats").html(compiledTemplate);
            return this;
          }

        });

        return ContactsStatsView;
      });
