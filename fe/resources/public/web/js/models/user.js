(function($) {
    
    window.User = Backbone.Model.extend({
        defaults: {
            'service': null,  //Facebook , LinkedIn, Gmail etc
            'state': 'LOGGED_OUT'
        },

        login: function(service){
            this.set({'service':service, 'state':'LOGGED_IN'});
        },

        logout: function(){
            this.set({'service':null, 'state':'LOGGED_OUT'});
        },

        service: function(){
            return this.get('service');
        },

        isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
        }
    });

})(jQuery);