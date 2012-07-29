(function($) {
    
    window.Friends = Backbone.Collection.extend({

        model: Friend,

        url: "http://localhost:4000/friends",

        sync: CustomBackbone.zoloSync,

        parse: function(response) {
            console.log(response);
            return response;
        }

    });
    

})(jQuery);