// Multi-line chart on http://bl.ocks.org/mbostock/3883245

// Declare variables with global scope so they appear in Firefox's debugger
// Data values

// Sample values
var values = [ [0.0,40.0], [1.0,40.0], [2.0, 38.0], [3.0, 38.0], [4.0, 25.0],
              [10.0, 9.0], [2.0, 7.0], [1.0, 5.0], [0.0, 10.0], [0.0, 40.0]];

// The SVG element in the HTML page
var svg;

// Graph dimensions.  Assumes a fixed-size object
var graphHeight = 350,
	graphWidth = 400;

// Whitespace padding around graph, to leave space for axis labels
var padding = 25; 

// Graph scales, axes, and lines
var xScale, 
	yScale,
	xAxis,
	yAxis,
	lineFunction,
	lineGroups,
	lines;

// Post parameters
var preyGrowthrate,
	predatorGrowthRate,
	meetRate,
	predatorLossRate;

// Function executed once page is loaded
$(document).ready(function() {	
	createGraph();
	
	// Register click handlers
	$("#calculate").click(calculate);
		
	// Hide loading message
	$("#loading").hide(); 
	
	// Create the progress bar (in hidden div "loading") 
	// http://jqueryui.com/progressbar/#indeterminate
	 $("#progressBar" ).progressbar({ value: false });
	 $("#progressBar").progressbar("option", "value", false);
});

// Create an SVG element showing the temperature graph
function createGraph() {

	// Create the SVG element to hold the graph
	svg = d3.select('#graph')
				.append('svg')
				.attr('height', graphHeight)
				.attr('width', graphWidth);
	
	// X axis is a linear axis; domain calculated from data
	xScale = d3.scale.linear()
		.domain([-0.3, d3.max(values, function(d) {return d[0]})])
		.range([3*padding, graphWidth - padding]);
	
	// Y axis is a linear axis; domain calculated from data
	yScale = d3.scale.linear()
		.domain([d3.max(values, function(d) {return d[1]}),-0.3])
		.range([padding, graphHeight - 3*padding]);
	
	// Draw axes
	// http://alignedleft.com/tutorials/d3/axes/

	xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom")
		.ticks(6);
	
	svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0, " + (graphHeight - 3*padding) + ")")
		.call(xAxis);
	
	// Add axis label.  Use unicode representation for degree symbol in text
	// http://alignedleft.com/tutorials/d3/axes/
	// http://www.d3noob.org/2012/12/adding-axis-labels-to-d3js-graph.html
	
	svg.append("text")
		.attr("class", "axisLabel")
		.attr("x", graphWidth / 2)
		.attr("y", graphHeight - padding)
		.style("text-anchor", "middle")
		.text("Predators");
	
	yAxis = d3.svg.axis()
		.scale(yScale)
		.orient("left")
		.ticks(6);
	
	svg.append("g")
		.attr("class", "y axis")
		.attr("transform", "translate(" + (3*padding) + ", 0)")
		.call(yAxis);

	svg.append("text")
		.attr("class", "axisLabel")
		.attr("x", -(graphHeight / 2) + padding)
		.attr("y", padding)
		.attr("transform", "rotate(-90)")
		.style("text-anchor", "middle")
		.text("Prey");
	
	lineFunction = d3.svg.line()
		.interpolate("basis")	// smooth line
		.x(function(d) {return xScale(d[0]); })
		.y(function(d) {return yScale(d[1]); });
	
	/*
	lines = svg.append("path")
		// Use [values] here to associate the entire array with a 
		// single path (i.e. line)
		// http://stackoverflow.com/questions/14958825/dynamically-update-chart-data-in-d3
		.data([values])
		.attr("class", "line")
		.attr("d", lineFunction)
		.attr("stroke-width", 2)
		.attr("stroke", "steelblue")
		.attr("fill", "none");
		*/
}

function calculate(){
	// Post the parameters to the server.  Kepler will calculate and return
	// the results
	preyGrowthRate = $("#preyGrowthRate").val();
	predatorGrowthRate = $("#predatorGrowthRate").val();
	meetRate = $("#meetRate").val();
	predatorLossRate = $("#predatorLossRate").val();
	
	// TODO:  Add loading dialog
	// Create wait dialog message
	$("#loadingMessage").html("Calculating...");

	// Issue post request
	$.ajax({
		url: 'predatorprey',
		type: 'POST',
		dataType: 'json',
		data: {preyGrowthRate: preyGrowthRate, 
			   predatorGrowthRate: predatorGrowthRate,
			   meetRate: meetRate,
			   predatorLossRate: predatorLossRate},
		success: function(result) {
			values = result;
			
			// Open graph page
			// TODO:  Develop big-screen and mobile-screen versions of demo
			// Use feature detection or media queries (or both) 
			// $.mobile.changePage($("#graphPage"));
				
			// Remove old line, and draw new line
			// FIXME:  Way to update old line?  Tried, but doesn't seem to work
			// TODO:  Allow multiple lines on the graph
			if (lines != null) {
				 lines.style("opacity", 1)
				 .transition().duration(600).style("opacity", 0).remove(); 
			}
			
			// Rescale axes
			xScale.domain([-0.3, d3.max(values, function(d) {return d[0]})]);
			yScale.domain([d3.max(values, function(d) {return d[1]}),-0.3]);
			
			// http://bl.ocks.org/mbostock/1166403
		    var t = svg.transition().duration(750);
		    t.select(".x.axis").call(xAxis);
		    t.select(".y.axis").call(yAxis);
		    
			
			lines = svg.append("path")
				.data([values])
				.attr("class", "line")
				.attr("d", lineFunction)
				.attr("stroke-width", 2)
				.attr("stroke", "steelblue")
				.attr("fill", "none")
				.style("opacity", 0);
			
			lines.transition().duration(600).style("opacity", 1);
		},
		error: function(e) {
			alert("Error running server-side simulation");
			// alert(JSON.stringify(e));
		}
	});
}

// Grey out page while loading.  See:
// http://stackoverflow.com/questions/1964839/jquery-please-wait-loading-animation

$(document).ajaxStart(function() { 
	 $("#loading").show();
});

$(document).ajaxStop(function() { 
     $("#loading").hide(); 
});

