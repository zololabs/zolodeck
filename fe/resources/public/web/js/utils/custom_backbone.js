var CustomBackbone = {

    zoloSync : function(method, model, options) {
        
        options.beforeSend = function(jqXHR) {
            
            jqXHR.setRequestHeader("Accept", "application/vnd.zololabs.zolodeck.v1+json");
  //          jqXHR.setRequestHeader("Authorization", "FB " + Facebook.signed_request());
            jqXHR.setRequestHeader("Access-Control-Allow-Origin", "*");
       
        }
        
        // Call the default Backbone sync implementation
        Backbone.sync.call(this, method, model, options);  
    }
    
};
