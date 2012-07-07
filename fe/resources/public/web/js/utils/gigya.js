var Gigya = {

    onLoginHandler: function(eventObj){
        var user  = this.context;
        user.login(eventObj.user);
    },

    logout: function(){
        window.currentUser.logout();
        gigya.services.socialize.logout();
    },
    
    onLogoutHandler: function(eventObj){
        //This is a listener function for gigya service logout. 
        //Currently we do not need to do anything as we do what we
        //want to do in logout function itself
    },
    
};
