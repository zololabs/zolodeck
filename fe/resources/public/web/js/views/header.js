define(['jquery',
        'underscore',
        'backbone',
        'text!templates/header.html'],

      function($, _, Backbone, headerTemplate){
        
        var HeaderView = Backbone.View.extend({
          
          el: $(".header"),

          events: {
            'click #logout' : 'logoutUsingGigya'
          },

          initialize:function () {
            _.bindAll(this, 'render', 'logoutUsingGigya');
            
            this.user = this.model;
            this.user.bind('change:state', this.render)
          },

          render: function(){
            var data = {};
            var compiledTemplate = _.template( headerTemplate, data );

            this.$el.html(compiledTemplate);
            this.$el.find(".after-login").toggle(this.user.isLoggedIn());

            return this;
          },

          logoutUsingGigya: function(){
            console.log("Logout Pressed");
            gigya.socialize.logout();
          }
        });

        return HeaderView;
      });
