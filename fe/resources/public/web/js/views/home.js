(function($) {
    window.HomeView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this, 'render');
            this.user = this.model;
            this.template = _.template(tpl.get('home'));
        },
        
        render: function (eventName) {
            console.log('Rendering Home');
            this.friendsListView = new FriendsListView({model:this.user.friends()});
            
            $(this.el).html(this.template());
            //$(this.el).find("#friends-list").append(this.friendsListView.render().el);
           
            return this;
        }  
      
    });
})(jQuery);