(function($) {
    window.currentUser = new User();

    window.facebook = new FacebookService({
        'user' : window.currentUser
    });

    window.Zolodeck = Backbone.Router.extend({

        routes:{
            ""         : "zolodeck"
        },
        
        initialize:function () {
            this.zolodeckView = new ZolodeckView({
                model: window.currentUser
            });

            this.headerView = new HeaderView({
                model: window.currentUser
            });

        },
        
        zolodeck:function () {
            $('.header').html(this.headerView.render().el);
            this.zolodeckView.render();
        }

    });
    
    $(document).ready(function () {	
        tpl.loadTemplates(['landing', 'header', 'home', 'friends-list-item', 'contacts_stats', 'network_stats'],
    	                  function () {
        	              app = new Zolodeck();
        	              Backbone.history.start();
    		          });
    });

})(jQuery);
