(function($) {
    window.HeaderView = Backbone.View.extend({
        events: {
            'click #logout' : 'logoutUsingFacebook'
        },
        
        initialize:function () {
            _.bindAll(this, 'render', 'logoutUsingFacebook');
            this.model.bind('change:state', this.render)
            this.template = _.template(tpl.get('header'));
        },
        
        render:function (eventName) {
            console.log('Rendering Header');
            var user = this.model;

            $(this.el).html(this.template());
            $(this.el).find(".after-login").toggle(user.isLoggedIn());

            return this;
        },

        logoutUsingFacebook: function(){
            FB.logout(function(response) {           
                console.log("Logged Out successfully");
            });
        }
        
    });
})(jQuery);


