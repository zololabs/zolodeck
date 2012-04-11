(function($){
    
    window.Friend = Backbone.Model.extend({

        initialize: function(){
            this.set({fbSmallImageUrl: "//graph.facebook.com/"+ this.get("id") + "/picture?type=small"});
            this.set({fbLargeImageUrl: "//graph.facebook.com/"+ this.get("id") + "/picture?type=large"});
        }

    });

})(jQuery);
