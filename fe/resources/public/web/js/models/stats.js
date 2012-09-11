define(['jquery',
        'underscore',
        'backbone',
        'utils/custom_backbone'],

      function($, _, Backbone, CustomBackbone){
        
        var StatsModel = Backbone.Model.extend({
          
          urlRoot: "http://localhost:4000/user-stats",
          
          sync: CustomBackbone.zoloSync,
          
          parse: function(response) {
            return response;
          },
          
          contacts: function() {
            return this.get('contacts');
          },
          
          network: function() {
            return this.get('network');
          }

        });
        
        return StatsModel;
      });
