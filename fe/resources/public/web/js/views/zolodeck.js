define(['jquery',
        'underscore',
        'backbone',
        'views/landing',
        'views/home'], 
       
       function($, _, Backbone, LandingView, HomeView){
         
         var ZolodeckView = Backbone.View.extend({

           el: $('#container'),
    
           initialize: function () {
             _.bindAll(this, 'render');
             
             this.user = this.model;
             this.user.bind('change:state', this.render);                 
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
             var homeView = new HomeView({model: this.user});
             homeView.render();
           },
           
           renderLanding: function(){
             var landingView = new LandingView({model: this.user});
             landingView.render();
           }
           
         });

         return ZolodeckView;
       });
