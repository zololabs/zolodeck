define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
        var LinkedinLoginView = Backbone.View.extend({

          
          initialize:function () {
            _.bindAll(this, 'render');
            
            this.user = this.model;
          },

          render: function(){
            
            var user = this.user;
            
            $(document).ready(function() {
              $.getScript("http://platform.linkedin.com/in.js?async=true", function success() {
                  IN.init({
                    api_key: 'p8rw9l9pzvl8',
                    authorize: true,
                    credentials_cookie: true
                  });
              });
            });

            return this;
          }

        });

        return LinkedinLoginView;
      });
