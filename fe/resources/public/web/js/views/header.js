(function($) {
    window.HeaderView = Backbone.View.extend({
        
        initialize:function () {
            _.bindAll(this, 'render');
            this.model.bind('change:state', this.render)
            this.template = _.template(tpl.get('header'));
        },
        
        render:function (eventName) {
            console.log('Rendering Header');
            var user = this.model;
            $(this.el).html(this.template());
            $(this.el).find(".after-login").toggle(user.isLoggedIn());
            return this;
        }
        
    });
})(jQuery);


