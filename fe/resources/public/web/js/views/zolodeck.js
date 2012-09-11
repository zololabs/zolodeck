define(['jquery',
        'underscore',
        'backbone',
        'views/landing',
        'views/home'], 
       
       function($, _, Backbone, LandingView, HomeView){
         
         var ZolodeckView = Backbone.View.extend({

           el: $('#container'),
    
           initialize: function () {
             _.bindAll(this, 'render', 'renderHome', 'renderLanding');
             
             this.user = this.model;
             this.user.bind('change:state', this.render);     

             this.homeView = new HomeView({model: this.user});            
             this.landingView = new LandingView({model: this.user});
           },
           
           render: function(eventName){
             console.log("Rendering Zolodeck");
             console.log("User Logged in : " + this.user.isLoggedIn());
             if(this.user.isLoggedIn()){
               this.renderHome();
             }else{
               this.renderLanding();
             }
             return this;
           },
           
           renderHome: function(){
             console.log("Rendering Home");
             this.homeView.render();
           },
           
           renderLanding: function(){
             console.log("Rendering Landing");
             this.landingView.render();
           }
           
         });

         return ZolodeckView;
       });
