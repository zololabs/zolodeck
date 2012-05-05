(function($) {
    window.HomeView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this, 'render');
            this.user = this.model;
            this.template = _.template(tpl.get('home'));
        },
        
        render: function (eventName) {
            console.log('Rendering Home');
            //this.friendsListView = new FriendsListView({model:this.user.friends()});
            // this.vizualizerD3 = new VizualizerD3();
            // this.vizualizerD3.fetch();
            
            this.vizualizerD3View = new VizualizerD3View({model: this.user.contactStrengthsD3()});
            
            $(this.el).html(this.template());
            //$(this.el).find("#vizualizer").append(this.vizualizerD3View.render().el);
            //$(this.el).find("#friends-list").append(this.friendsListView.render().el);
           
            return this;
        }  
      
    });
})(jQuery);