(function($) {
    window.currentUser = new User();

    window.facebook = new FacebookService({
        'user' : window.currentUser
    });

    window.Zolodeck = Backbone.Router.extend({

        routes:{
            "":"home"
        },
        
        initialize:function () {
            this.headerView = new HeaderView({
                model: window.currentUser
            });
        },
        
        home:function () {
            // Since the home view never changes, we instantiate it and render it only once
            if (!this.homeView) {
                this.homeView = new HomeView({
                    model: window.currentUser
                });
                this.homeView.render();
            }
            $('.header').html(this.headerView.render().el);
            $('#content').html(this.homeView.el);
        }
        
    });
    
    $(document).ready(function () {	
        tpl.loadTemplates(['home', 'header_user', 'header_guest'],
    	                  function () {
        	              app = new Zolodeck();
        	              Backbone.history.start();
    		          });
    });

})(jQuery);
