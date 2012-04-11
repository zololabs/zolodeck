(function($) {
    window.currentUser = new User();

    window.facebook = new FacebookService({
        'user' : window.currentUser
    });

    window.Zolodeck = Backbone.Router.extend({

        routes:{
            ""         : "home"
        },
        
        initialize:function () {
            this.homeView = new HomeView({
                model: window.currentUser
            });
            this.headerView = new HeaderView({
                model: window.currentUser
            });
        },
        
        home:function () {
            $('.header').html(this.headerView.render().el);
            $('#content').html(this.homeView.render().el);
        }

    });
    
    $(document).ready(function () {	
        tpl.loadTemplates(['home', 'header', 'friends-list-item'],
    	                  function () {
        	              app = new Zolodeck();
        	              Backbone.history.start();
    		          });
    });

})(jQuery);
