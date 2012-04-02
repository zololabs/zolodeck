var Facebook = {

    appId      : '361942873847116',

    setup: function(){
        window.fbAsyncInit = function() {
            FB.init({
                appId      : Facebook.appId,
                //channelUrl : '//WWW.YOUR_DOMAIN.COM/channel.html', // Channel File
                status     : true, // check login status
                cookie     : true, // enable cookies to allow the server to access the session
                xfbml      : true  // parse XFBML
            });
            
            FB.Event.subscribe('auth.statusChange', function(response) {
                console.log("auth.statusChange", response);
            });
        };
        
        // Load the SDK Asynchronously
        (function(d){
            var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
            if (d.getElementById(id)) {return;}
            js = d.createElement('script'); js.id = id; js.async = true;
            js.src = "//connect.facebook.net/en_US/all.js";
            ref.parentNode.insertBefore(js, ref);
        }(document));
    }
    
};