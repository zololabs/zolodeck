(function($) {
    
    window.User = Backbone.Model.extend({
        defaults: {
            'service': null,  //Facebook , LinkedIn, Gmail etc
            'state': 'LOGGED_OUT',
            'friends': new Friends()
        },

        friends: function(){
            return this.get('friends');
        },

        login: function(service){
            console.log("Logged In : " , service);
            this.set({'service':service, 'state':'LOGGED_IN'});
            this.friends().fetch();
            this.set({'friends':this.friends()});
        },

        logout: function(){
            console.log("Logged Out");
            this.set({'service':null, 'state':'LOGGED_OUT', 'friends' : new Friends()});
        },

        service: function(){
            return this.get('service');
        },

        isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
        }

    });

})(jQuery);