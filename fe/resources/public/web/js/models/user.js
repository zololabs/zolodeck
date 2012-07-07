(function($) {
    
    window.User = Backbone.Model.extend({
        defaults: {
            'state': 'LOGGED_OUT',
            'contactStrengthsD3':  new VisualizerD3()
        },

        contactStrengthsD3: function(){
            return this.get('contactStrengthsD3');
        },
        
        signup: function(){
            console.log("Sign Up as he a new User");
        },

        login: function(gigyaUser){
            this.set(gigyaUser);
            this.set({'state':'LOGGED_IN'});
            if(this.isNewUser()){
                this.signup()
            }
            // this.contactStrengthsD3().fetch();
            // this.set({'contactStrengthsD3':this.contactStrengthsD3()});
        },

        logout: function(){
            console.log("Logged Out");
            this.set({'state':'LOGGED_OUT', 
                      'contactStrengthsD3':  new VisualizerD3()});
        },

        isLoggedIn: function(){
            return (this.get('state') == 'LOGGED_IN');
        },

        isLoggedOut: function(){
            return !this.isLoggedIn();
        },

        isNewUser: function(){
            var siteUID = this.get('isSiteUID');
            return ((siteUID == null) || (siteUID == false));
        }

    });

})(jQuery);
