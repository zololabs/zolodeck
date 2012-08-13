define(['jquery', 
        'underscore', 
        'backbone', 
        'models/user',
        'views/header',
        'views/zolodeck'], 
       
       function($, _, Backbone, User, HeaderView, ZolodeckView){

         var AppRouter = Backbone.Router.extend({
    
           routes:{
             ""         : "zolodeck"
           },
           
           zolodeck:function () {
             var user = new User;

             var headerView = new HeaderView({model: user});
             headerView.render();

             var zolodeckView = new ZolodeckView({model: user});
             zolodeckView.render();
           }
         });

         var initialize = function(){
           var appRouter = new AppRouter;
           Backbone.history.start();
         };
         
         return {
           initialize: initialize
         };

       });
