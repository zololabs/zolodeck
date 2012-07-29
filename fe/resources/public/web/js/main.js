require.config({

  paths: {
    jquery: 'libs/jquery/jquery-1.7.1.min',
    underscore: 'libs/underscore/underscore-min',
    backbone: 'libs/backbone/backbone-min'
  },

  shim: {

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
require(['jquery', 'underscore', 'backbone', 'app'], 
        function ($, _, Backbone, App) {   
          
          App.initialize();
          
        });
