function spatial_impact(element, rawData) {
    var summ = 0;
    rawData.distribution.forEach(function(d) {
        summ += d;
    });

    var i = -1;
    var data = rawData.distribution.map(function(d) {
        i += 0.01;
        return {
            x: i,
            y: d / summ
        }
    });

    // sort markers on their value
    var sortable = [];
    for (var city in rawData.cities)
        sortable.push([city, rawData.cities[city]]);
    sortable.sort(function(a, b) {return a[1] - b[1]});

    var markers = sortable.map(function(value) {
        return {
            city: value[0],
            x: value[1]
        }
    });

    $(element).html("");
    makeChart(element, data, markers);
}

function makeChart(element, data, markers) {

    var svgWidth = 960,
        svgHeight = 500,
        margin = {
            top: 20,
            right: 20,
            bottom: 40,
            left: 60
        },
        chartWidth = svgWidth - margin.left - margin.right,
        chartHeight = svgHeight - margin.top - margin.bottom;

    var x = d3.scale.linear().range([0, chartWidth]).domain(d3.extent(data, function(d) {
            return d.x
        })),
        y = d3.scale.linear().range([chartHeight, 0]).domain(d3.extent(data, function(d) {
            return d.y
        }));

    var xAxis = d3.svg.axis().scale(x).orient('bottom').outerTickSize(0).tickPadding(10),
        yAxis = d3.svg.axis().scale(y).orient('left').outerTickSize(0).tickPadding(10);

    var svg = d3.select(element)
        .append('svg')
        .attr('width', svgWidth)
        .attr('height', svgHeight)
        .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    var rectClip = svg.append('clipPath')
        .attr('id', 'rect-clip')
        .append('rect')
        .attr('width', 0)
        .attr('height', chartHeight);

    addAxesAndLegend(svg, xAxis, yAxis, margin, chartWidth, chartHeight);
    drawPaths(svg, data, x, y, svgHeight);
    startTransitions(svg, chartWidth, chartHeight, rectClip, markers, x);
}

function addAxesAndLegend(svg, xAxis, yAxis, margin, chartWidth, chartHeight) {

	var axes = svg.append('g').attr('clip-path', 'url(#axes-clip)');

	axes.append('g')
	    .attr('class', 'x axis')
	    .attr('transform', 'translate(0,' + chartHeight + ')')
	    .call(xAxis);

	axes.append('g')
	    .attr('class', 'y axis')
	    .call(yAxis).append('text')
	    .attr('transform', 'rotate(-90)')
	    .attr('y', 6).attr('dy', '.71em')
	    .style('text-anchor', 'end')
        .attr("class", "label")
	    .text('Distribution');
}

function drawPaths(svg, data, x, y, svgHeight) {

	var lowerOuterArea = d3.svg.area().interpolate('basis').x(function(d) {
		return x(d.x) || 1;
	}).y0(function(d) {
		return y(d.y);
	}).y1(function(d) {
		return svgHeight;
	});

	svg.datum(data);
	svg.append('path')
	    .attr('class', 'area lower outer')
	    .attr('d', lowerOuterArea)
	    .attr('clip-path', 'url(#rect-clip)');
}

function addMarker(marker, svg, chartHeight, x, height) {

	var radius = 42,
		xPos = x(marker.x * 0.01 - 1) - radius - 3,
		yPosStart = chartHeight - radius - 3,
		yPosEnd = height + radius - 3;

	var markerG = svg.append('g')
	    .attr('class', 'marker color')
	    .attr('transform', 'translate(' + xPos + ', ' + yPosStart + ')')
	    .attr('opacity', 0);

	markerG.transition().duration(1000)
	    .attr('transform', 'translate(' + xPos + ', ' + yPosEnd + ')')
	    .attr('opacity', 1);

	markerG.append('path')
	    .attr('d', 'M' + radius + ',' + (chartHeight - yPosStart) + 'L' + radius + ',' + (chartHeight - yPosStart))
	    .transition()
	    .duration(1000)
	    .attr('d', 'M' + radius + ',' + (chartHeight - yPosEnd) + 'L' + radius + ',' + (radius * 2));

	markerG.append('circle')
	    .attr('class', 'marker-bg')
	    .attr('cx', radius).attr('cy', radius)
	    .attr('r', radius);

	markerG.append('text')
	    .attr('x', radius)
	    .attr('y', radius * 1.1)
	    .text(marker.city);
}

function startTransitions(svg, chartWidth, chartHeight, rectClip, markers, x) {

	rectClip.transition().duration(1000 * markers.length).attr('width', chartWidth);

    var heights = [10, 110, 210, 310];
	markers.forEach(function(marker, i) {
		setTimeout(function() {
			addMarker(marker, svg, chartHeight, x, heights[i % heights.length]);
		}, 1000 + 500 * i);
	});
}