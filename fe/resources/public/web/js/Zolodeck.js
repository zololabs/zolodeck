(function($) {
    window.currentUser = new User();

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
        tpl.loadTemplates(['landing', 'header', 'home'],
    	                  function () {
        	              app = new Zolodeck();
        	              Backbone.history.start();
    		          });
    });

})(jQuery);
