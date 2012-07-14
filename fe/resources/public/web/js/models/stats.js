(function($) {

    window.Stats = Backbone.Model.extend({
        url: "http://localhost:4000/user-stats",
        
        sync: CustomBackbone.zoloSync,

        parse: function(response) {
            return response;
        },

        contacts: function() {
            return this.get('contacts');
        },

        network: function() {
            return this.get('network');
        }

    });

})(jQuery);
