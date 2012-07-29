(function($) {

    window.VisualizerD3View = Backbone.View.extend({
        
        id: "graph",

        initialize:function () {
            var self = this;
            
            this.contactStrengthsD3 = this.model;

            this.model.bind("change", this.render, this);
        },

        renderForceGraph:function(){
            var width = 960;
            var height = 900;
            var r = 5;
            
            var color = d3.scale.category20();
            
            var force = d3.layout.force()
                .charge(-500)
                .linkDistance(function(link, index) { return link.value; })
                .size([width, height]);
            
            var svg = d3.select(this.el).append("svg")
                .attr("width", width)
                .attr("height", height);

            var loadingText = svg.append("svg:text").attr("class", "loading")
                .attr("x", width/2)
                .attr("y", height/2)
                .text("Loading");
           
            var json = (this.contactStrengthsD3.toJSON());

            force
                .nodes(json.nodes)
                .links(json.links)
                .start();

            var link = svg.selectAll("line.link")
                .data(json.links)
                .enter().append("line")
                .attr("class", "link");

            var node = svg.selectAll("circle.node")
                .data(json.nodes)
                .enter().append("circle")
                .attr("class", "node")
                .attr("r", function(d) { if(d.center) {
                    return (r + 10 ); 
                } else {
                    return r;}})
                .style("fill", function(d) { return color(d.group); })
                .call(force.drag);

            //To add Image It is still not working
            // node.append("svg:image")
            //     .attr("class", "circle")
            //     .attr("xlink:href", "https://d3nwyuy0nl342s.cloudfront.net/images/icons/public.png")
            //     .attr("x", "-8px")
            //     .attr("y", "-8px")
            //     .attr("width", "16px")
            //     .attr("height", "16px");
            
            node.append("title")
                .text(function(d) { return d.name; });
            
            var initialized = false;

            force.on("tick", function(e) {

                if(e.alpha < 0.01 && initialized == false || initialized == true){
                    link.attr("x1", function(d) { return d.source.x; })
                        .attr("y1", function(d) { return d.source.y; })
                        .attr("x2", function(d) { return d.target.x; })
                        .attr("y2", function(d) { return d.target.y; });
                    
                    // To keep it in bounded box NOT working
                    // node.attr("cx", function(d) { return d.x = Math.max(r, Math.min(w - r, d.x)); })
                    // .attr("cy", function(d) { return d.y = Math.max(r, Math.min(h - r, d.y)); });
                    node.attr("cx", function(d) { return d.x; })
                        .attr("cy", function(d) { return d.y; });

                    if(initialized == false){
                        svg.select(".loading").remove();
                        initialized = true;
                    }
                } else {
                    loadingText.text(function(){return "Loading: " + Math.round((1 - (e.alpha * 10 - 0.1)) * 100) + "%"});
                }
            });

            //Apply FishEye
            var fisheye = d3.fisheye()
                .radius(200)
                .power(2);

            svg.on("mousemove", function() {
                fisheye.center(d3.mouse(this));
                
                node.each(function(d) { d.display = fisheye(d); })
                    .attr("cx", function(d) { return d.display.x; })
                    .attr("cy", function(d) { return d.display.y; })
                    .attr("r", function(d) { if(d.center) {
                        return (r + 10); 
                    } else {
                        return d.display.z * 4.5;}});
                
                link.attr("x1", function(d) { return d.source.display.x; })
                    .attr("y1", function(d) { return d.source.display.y; })
                    .attr("x2", function(d) { return d.target.display.x; })
                    .attr("y2", function(d) { return d.target.display.y; });
            });
            
            return this.el;
        },
        
        render:function () {
            console.log("Rendering Vizualizer D3");
            $("#visualizer").append(this.renderForceGraph());
            return this;
        }
        
    });

})(jQuery);


