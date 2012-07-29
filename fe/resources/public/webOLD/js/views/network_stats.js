(function($) {
    window.NetworkStatsView = Backbone.View.extend({
        
        initialize: function () {
            _.bindAll(this, 'render');

            this.stats = this.model;

            this.template = _.template(tpl.get('network_stats'));

            this.stats.bind("change", this.render, this);
        },
        
        render: function (eventName) {
            console.log('Rendering Network Stats');
            
            $("#network-stats").html(this.template(this.stats.network()));
            
            return this;
        }

    });
})(jQuery);


