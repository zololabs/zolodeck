(function($) {
    window.HeaderView = Backbone.View.extend({
        events: {
            'click #logout' : 'logoutUsingGigya'
        },
        
        initialize:function () {
            _.bindAll(this, 'render', 'logoutUsingGigya');

            this.user = this.model;
            this.model.bind('change:state', this.render)

            this.template = _.template(tpl.get('header'));
        },
        
        render:function (eventName) {
            console.log('Rendering Header');

            $(this.el).html(this.template());
            $(this.el).find(".after-login").toggle(this.user.isLoggedIn());

            return this;
        },

        logoutUsingGigya: function(){
            console.log("Logout Pressed");
            Gigya.logout();
        }
        
    });
})(jQuery);


