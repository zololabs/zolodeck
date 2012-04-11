var Facebook = {

    appId      : '361942873847116',

    signed_request : function(){
        return Cookies.read_cookie("fbsr_" + Facebook.appId);
    }
    
};