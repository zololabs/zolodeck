var Gigya = {

    onLoginHandler: function(eventObj){
        var user  = this.context;
        user.login(eventObj.user);
    },

    logout: function(){
        gigya.services.socialize.logout();
    },
    
    onLogoutHandler: function(eventObj){
        window.currentUser.logout();
    },
    
};
