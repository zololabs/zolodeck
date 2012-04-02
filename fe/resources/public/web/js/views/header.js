(function($) {
    window.HeaderView = Backbone.View.extend({
        
        initialize:function () {
            _.bindAll(this, 'render');
            this.model.bind('change:state', this.render)
            this.guest_template = _.template(tpl.get('header_guest'));
            this.user_template = _.template(tpl.get('header_user'));
        },
        
        render:function (eventName) {
            console.log('Rendering Header');
            var user = this.model;
            if (user.isLoggedIn()){
                $(this.el).html(this.user_template());
            } else {
                $(this.el).html(this.guest_template());
            }
            return this;
        }
        
    });
})(jQuery);