(function($) {
    window.HomeView = Backbone.View.extend({
        events: {
            'click #facebook_login': 'loginUsingFacebook'
        },
        
        initialize: function () {
            console.log('Initializing Home View');
            _.bindAll(this, 'render', 'loginUsingFacebook');
            this.template = _.template(tpl.get('home'));
        },
        
        render: function (eventName) {
            $(this.el).html(this.template());
            return this;
        },

        loginUsingFacebook: function(){
            var that = this;
            FB.login(function(response){
                if (response.authResponse){
                    that.model.logIn("FACEBOOK");
                }else{
                    opts.error(response);
                }
            },{scope : 'email,friends_about_me,friends_birthday,friends_relationship_details,friends_location,friends_likes,friends_website'});
        }
        
    });
})(jQuery);