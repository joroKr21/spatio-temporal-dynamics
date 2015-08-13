function entropy_focus_spread(element, data) {
    var margin  = {top: 20, right: 20, bottom: 30, left: 40},
        width   = 960 - margin.left - margin.right,
        height  = 500 - margin.top - margin.bottom;

    /*
     * value accessor - returns the value to encode for a given data object.
     * scale - maps value to a visual display encoding, such as a pixel position.
     * map function - maps from data value to display value
     * axis - sets up axis
     */

    // setup x
    var xValue  = function(d) { return d['focus'];}, // data -> value
        xScale  = d3.scale.linear().range([0, width]), // value -> display
        xMap    = function(d) { return xScale(xValue(d));}, // data -> display
        xAxis   = d3.svg.axis().scale(xScale).orient("bottom");

    // setup y
    var yValue  = function(d) { return d["entropy"];}, // data -> value
        yScale  = d3.scale.linear().range([height, 0]), // value -> display
        yMap    = function(d) { return yScale(yValue(d));}, // data -> display
        yAxis   = d3.svg.axis().scale(yScale).orient("left");

    // setup fill color
    var cValue  = function(d) { return d.spread;},
        color   = d3.scale.linear()
                    .domain([0, 3000])
                    .range(["red", "yellow"]);

    // setup radius
    var rValue      = function(d) { return d.occurrences},
        max_radius  = d3.max(data.map(function (d) { return rValue(d); })),
        radius      = d3.scale.linear()
                        .domain([0, max_radius])
                        .range([3, 9]);

    // Clear contents of element and legend-element
    var legend_el = "#linearLegend";
    $(legend_el).html("");
    $(element).html("");

    // add the graph canvas to the body of the webpage
    var svg = d3.select(element)
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // add the tooltip area to the webpage
    var tooltip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);


    // don't want dots overlapping axis, so add in buffer to data domain
    xScale.domain([d3.min(data, xValue)-1, d3.max(data, xValue)+1]);
    yScale.domain([d3.min(data, yValue)-1, d3.max(data, yValue)+1]);

    // x-axis
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .append("text")
        .attr("class", "label")
        .attr("x", width)
        .attr("y", -6)
        .style("text-anchor", "end")
        .text("Mean hashtag focus");

    // y-axis
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Mean hashtag entropy");

    // draw dots
    var dots = svg.selectAll(".dot")
        .data(data)
        .enter().append("circle")
        .attr("class", "dot")
        .attr("r", 0)
        .attr("cx", xMap)
        .attr("cy", yMap)
        .style("fill", function(d) { return color(cValue(d));})
        .style("opacity", 0.9)
        .on("mouseover", function(d) {
            tooltip.transition()
                .duration(200)
                .style("opacity", .9);
            tooltip.html(
                "<h5>#" + d["tag"] + "</h5>" +
                "<div>Occurrences: <i>" + rValue(d) + "</i></div>" +
                "<div>Focus: <i>" + xValue(d) + "</i></div>" +
                "<div>Entropy: <i>" + yValue(d) + "</i></div>" +
                "<div>Spread: <i>" + cValue(d) + "</i></div>"
            )
                .style("left", (d3.event.pageX + 5) + "px")
                .style("top", (d3.event.pageY - 28) + "px");
        })
        .on("mouseout", function(d) {
            tooltip.transition()
                .duration(500)
                .style("opacity", 0);
        });

    dots.transition().duration(1500).attr("r", function(d) { return radius(rValue(d)); });

    colorlegend("#linearLegend", color, "linear", {title: "linear"});
}