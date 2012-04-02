(function($) {
    
    window.User = Backbone.Model.extend({
        defaults: {
            'service': null,  //Facebook , LinkedIn, Gmail etc
            'state': 'LOGGED_OUT'
        },

        logIn: function(service){
            this.set({'service':service, 'state':'LOGGED_IN'});
            console.log("Logged in using " , service, this);
        },

        logOut: function(){
        },

        isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
        }
    });

})(jQuery);