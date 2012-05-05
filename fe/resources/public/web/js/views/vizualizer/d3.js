(function($) {

    window.VizualizerD3View = Backbone.View.extend({

        initialize:function () {
            var self = this;
            
            this.contactStrengthsD3 = this.model;

            this.model.bind("change", this.render, this);
        },

        renderForce:function(){
            var width = 960, height = 900;
            
            var color = d3.scale.category20();
            
            var force = d3.layout.force()
                .charge(-500)
                .linkDistance(function(link, index) { return link.value; })
                .size([width, height]);
            
            var svg = d3.select("#vizualizer").append("svg")
                .attr("width", width)
                .attr("height", height);

           
            var json = (this.contactStrengthsD3.toJSON());

            force
                .nodes(json.nodes)
                .links(json.links)
                .start();

            var link = svg.selectAll("line.link")
                .data(json.links)
                .enter().append("line")
                .attr("class", "link");
            // .style("stroke-width", function(d) { return Math.sqrt(d.value); });

            var node = svg.selectAll("circle.node")
                .data(json.nodes)
                .enter().append("circle")
                .attr("class", "node")
                .attr("r", 20)
                .style("fill", function(d) { return color(d.group); })
                .call(force.drag);

            // node.append("svg:image")
            //     .attr("class", "circle")
            //     .attr("xlink:href", "https://d3nwyuy0nl342s.cloudfront.net/images/icons/public.png")
            //     .attr("x", "-8px")
            //     .attr("y", "-8px")
            //     .attr("width", "16px")
            //     .attr("height", "16px");

            node.append("title")
                .text(function(d) { return d.name; });

            force.on("tick", function() {
                link.attr("x1", function(d) { return d.source.x; })
                    .attr("y1", function(d) { return d.source.y; })
                    .attr("x2", function(d) { return d.target.x; })
                    .attr("y2", function(d) { return d.target.y; });
                
                node.attr("cx", function(d) { return d.x; })
                    .attr("cy", function(d) { return d.y; });
            });

        },
        
        render:function () {
            console.log("Rendering Vizualizer D3");
            this.renderForce();
            return this;
        }
        
    });

})(jQuery);


