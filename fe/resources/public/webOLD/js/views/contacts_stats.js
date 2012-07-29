(function($) {
    window.ContactsStatsView = Backbone.View.extend({
        
        initialize: function () {
            _.bindAll(this, 'render');

            this.stats = this.model;

            this.template = _.template(tpl.get('contacts_stats'));

            this.stats.bind("change", this.render, this);
        },
        
        render: function (eventName) {
            console.log('Rendering Contact Stats');
            
            $("#contacts-stats").html(this.template(this.stats.contacts()));
            
            return this;
        }

    });
})(jQuery);


