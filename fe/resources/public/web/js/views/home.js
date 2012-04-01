(function($) {
    window.HomeView = Backbone.View.extend({
        events: {
            'click #facebook_login': 'login_using_facebook'
        },
        
        initialize:function () {
            console.log('Initializing Home View');
            this.template = _.template(tpl.get('home'));
        },
        
        render:function (eventName) {
            $(this.el).html(this.template());
            return this;
        },

        login_using_facebook:function(){
            opts = {
                scope   : 'email,friends_about_me,friends_birthday,friends_relationship_details,friends_location,friends_likes,friends_website',
                success : function (response) { console.log("You are authorized"); console.dir(response);},
                error   : function (response) { console.log("You are not authorized : "); console.dir(response);}
            };
            
            FB.login(function(response){
                if (response.authResponse){
                    opts.success(response);
                }else{
                    opts.error(response);
                }
            },{scope : opts.scope});
        }
        
    });
})(jQuery);