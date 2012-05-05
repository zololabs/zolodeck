(function($){
    
    window.VisualizerD3 = Backbone.Model.extend({

        url: "http://localhost:4000/contact-strengths?client=d3",

        sync: CustomBackbone.zoloSync,

        parse: function(response) {
            return response;
        }

    });

})(jQuery);
