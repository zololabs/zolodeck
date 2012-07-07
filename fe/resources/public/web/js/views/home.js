(function($) {
    window.HomeView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this, 'render');
            this.user = this.model;
            this.template = _.template(tpl.get('home'));
        },
        
        render: function (eventName) {
            console.log('Rendering Home');
            this.visualizerD3View = new VisualizerD3View({model: this.user.contactStrengthsD3()});
            
            $(this.el).html(this.template());
           
            return this;
        }  
      
    });
})(jQuery);
