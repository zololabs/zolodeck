window.HomeView = Backbone.View.extend({

    initialize:function () {
        console.log('Initializing Home View');
        this.template = _.template(tpl.get('home'));
    },

    render:function (eventName) {
        $(this.el).html(this.template());
        return this;
    }

});