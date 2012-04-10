(function($) {

    window.FriendsListView = Backbone.View.extend({

        initialize:function () {
            var self = this;

            this.friends = this.model;

            this.friends.bind("reset", this.render, this);
            this.friends.bind("add", function(friend) {
                $(self.el).append(new FriendsListItemView({model:friend}).render().el);
            });
        },
        
        render:function (eventName) {
            console.log('Rendering FriendsListView');
            _.each(this.model.models, function(friend){
                $(this.el).append(new FriendsListItemView({model:friend}).render().el);
            },this);
            
            return this;
        }

    });


    
    window.FriendsListItemView = Backbone.View.extend({

        initialize:function () {
            this.template = _.template(tpl.get('friends-list-item'));
            this.model.bind("change", this.render, this);
        },
        
        render:function () {
            console.log('Rendering FriendsListItemView : ' +  this.model.get('name'));
            console.log(this.model.toJSON());
            console.log("ID : " + this.model.id);
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        }
        
    });

})(jQuery);


