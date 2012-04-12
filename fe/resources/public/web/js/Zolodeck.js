(function($) {
    window.currentUser = new User();

    window.facebook = new FacebookService({
        'user' : window.currentUser
    });

    window.Zolodeck = Backbone.Router.extend({

        routes:{
            ""         : "landing"
        },
        
        initialize:function () {
            this.landingView = new LandingView({
                model: window.currentUser
            });
            this.headerView = new HeaderView({
                model: window.currentUser
            });
        },
        
        landing:function () {
            $('.header').html(this.headerView.render().el);
            $('#content').html(this.landingView.render().el);
        }

    });
    
    $(document).ready(function () {	
        tpl.loadTemplates(['landing', 'header', 'friends-list-item'],
    	                  function () {
        	              app = new Zolodeck();
        	              Backbone.history.start();
    		          });
    });

})(jQuery);
