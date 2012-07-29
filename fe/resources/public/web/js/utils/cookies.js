define(['jquery',
        'underscore',
        'backbone'],

      function($, _, Backbone){
        
        return {
          
          delete_cookie: function(name) {
            this.create_cookie_till(name, null, 1);
          },
          
          create_cookie_till: function(name, value, ts) {
            var expires = "";
            if (ts) {
              var date = new Date();
              date.setTime(ts);
              expires = "; expires=" + date.toGMTString();
            }
            
            var url = window.location.hostname;
            document.cookie = name + "=" + value + expires + "; path=/ ; domain=" + url;
          },
          
          create_cookie_for_minutes: function(name, value, minutes) {
            var expires_at = null;
            if (minutes) {
              expires_at = (new Date()).getTime() + (minutes * 60 * 1000);
            }
            this.create_cookie_till(name, value, expires_at);
          },
          
          create_cookie_for_days: function(name, value, days) {
            if (days) {
              this.create_cookie_for_minutes(name, value, (days * 24 * 60));
            } else {
              this.create_cookie_for_minutes(name, value);
            }
          },
          
          read_cookie: function(name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i = i + 1) {
              var c = ca[i];
              while (c.charAt(0) === ' ') {
                c = c.substring(1, c.length);
              }
              if (c.indexOf(nameEQ) === 0) {
                return c.substring(nameEQ.length, c.length);
              }
            }
            return null;
          }
        };

      });
