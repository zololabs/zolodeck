(function($) {
    
    window.User = Backbone.Model.extend({
        defaults: {
            'service': null,  //Facebook , LinkedIn, Gmail etc
            'state': 'LOGGED_OUT',
            // 'friends': new Friends(),
            // 'contactStrengthsD3':  new VisualizerD3()
            'stats': new Stats()
        },

        friends: function(){
            return this.get('friends');
        },

        contactStrengthsD3: function(){
            return this.get('contactStrengthsD3');
        },

        stats: function() {
            return this.get('stats');
        },

        login: function(service){
            console.log("Logged In : " , service);
            this.set({'service':service, 'state':'LOGGED_IN'});
            //this.friends().fetch();
            this.set({'friends':this.friends()});
            //this.contactStrengthsD3().fetch();
            //this.set({'contactStrengthsD3':this.contactStrengthsD3()});
            this.stats().fetch();
        },

        logout: function(){
            console.log("Logged Out");
            this.set({'service':null, 
                      'state':'LOGGED_OUT', 
                      'friends' : new Friends(),
                      'contactStrengthsD3':  new VisualizerD3()});
        },

        service: function(){
            return this.get('service');
        },

        isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
        },

        isLoggedOut: function(){
            return !this.isLoggedIn();
        }

    });

})(jQuery);
