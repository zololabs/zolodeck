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
            console.log('Rendering Home');
            $(this.el).html(this.template());
            return this;
        },

        loginUsingFacebook: function(){
            var that = this;
            FB.login(function(response){
                if (response.authResponse){
                    // Not needed as we listen to Facebook AuthChange Event
                    // that.model.login("FACEBOOK"); 
                    console.log("Logged in successfully");
                }else{
                    console.log(response);
                }
            },{scope : 'email,friends_about_me,friends_birthday,friends_relationship_details,friends_location,friends_likes,friends_website'});
        }
        
    });
})(jQuery);