(function ($) {
    
    window.ZolodeckView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this, 'render');
            
            this.user = this.model;
            this.user.bind('change:state', this.render)            

        },

        render: function(eventName){
            console.log("Rendering Zolodeck");
            if(this.user.isLoggedIn()){
                this.renderHome();
            }else{
                this.renderLanding();
            }
            return this;
        },

        renderHome: function(){
            this.homeView = new HomeView({
                model: this.user
            });
            $('#content').html(this.homeView.render().el);
        },

        renderLanding: function(){
            this.landingView = new LandingView({
                model: this.user
            });
            $('#content').html(this.landingView.render().el);
        },

    });

})(jQuery);