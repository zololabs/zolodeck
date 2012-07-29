define(['jquery',
        'underscore',
        'backbone',
        'text!templates/network_stats.html'],

      function($, _, Backbone, networkStatsTemplate){
        
        var NetworkStatsView = Backbone.View.extend({
          
          initialize: function () {
            _.bindAll(this, 'render');
            
            this.stats = this.model;
            this.stats.bind("change", this.render, this);
          },
        
          render: function (eventName) {
            var data = this.stats.network();
            var compiledTemplate = _.template( networkStatsTemplate, data );

            $("#network-stats").html(compiledTemplate);
            return this;
          }

        });

        return NetworkStatsView;
      });
