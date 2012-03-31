var AppRouter = Backbone.Router.extend({

    routes:{
        "":"home"
    },

    initialize:function () {
        this.headerView = new HeaderView();
        $('.header').html(this.headerView.render().el);
    },

    home:function () {
        // Since the home view never changes, we instantiate it and render it only once
        if (!this.homeView) {
            this.homeView = new HomeView();
            this.homeView.render();
        }
        $('#content').html(this.homeView.el);
    }

});

$(document).ready(function () {	
    tpl.loadTemplates(['home', 'header'],
    	              function () {
        	          app = new AppRouter();
        	          Backbone.history.start();
    		      });
});
