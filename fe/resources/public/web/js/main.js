require.config({

  paths: {
    jquery: 'libs/jquery/jquery-1.7.1.min',
    underscore: 'libs/underscore/underscore-min',
    backbone: 'libs/backbone/backbone-min',
    cookies: 'libs/plugins/jquery_cookie/jquery.cookie'
  },

  shim: {

    //'libs/plugins/jquery_cookie/jquery.cookie': ['jquery'],

      cookies: ['jquery'],

      underscore: {
          exports: '_'
      },
      
      backbone: {
          'deps': ['jquery', 'underscore'],
          'exports': 'Backbone'
    }
      
  }	
    
});


//the "main" function to bootstrap your code
require(['jquery', 'cookies', 'underscore', 'backbone', 'app'], 
        function ($, jCookies, _, Backbone, App) {   
            App.initialize();
        });
