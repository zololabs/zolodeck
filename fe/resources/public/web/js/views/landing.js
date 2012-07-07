(function($) {
    window.LandingView = Backbone.View.extend({
        initialize: function () {
            _.bindAll(this, 'render');
            
            this.user = this.model;
            this.template = _.template(tpl.get('landing'));
        },
        
        render: function (eventName) {
            $(this.el).html(this.template());
            return this;
        }

    });
})(jQuery);
