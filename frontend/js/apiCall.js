

/*
window.onload = () => {
  'use strict';

  if ('serviceWorker' in navigator) {
    navigator.serviceWorker
             .register('js/serviceworker.js');
  }
}*/

Y({
	db: {
		name: 'memory' // use memory database adapter.
	},
	connector: {
		name: 'webrtc', // use webrtc connector
		room: 'query-room' // clients connecting to the same room share data 
	},
	sourceDir: '/bower_components', // location of the y-* modules (browser only)
	share: {
		map: 'Map'
	}
}).then(function (y) {
	window.y = y;
	console.log('Yjs instance ready!');
	y.share.map.delete("request");
	y.share.map.delete("requestSecond");
	//y.share.map.delete("requestType");
	y.share.map.delete("database");
	y.share.map.delete("vizType");
	//y.share.map.set("request", "");
	//y.share.map.set("requestType", document.getElementById("RequestType").value);
	//y.share.map.set("vizType", "setting");
	y.share.map.observe(function (event) {
		if (event.type === 'update' && document.getElementById("collabToggle").checked == true) {
			console.log("update");
			if (document.getElementById("Request").value == "") {
				let cleaned = y.share.map.get("request").replace(/['"]+/g, '')
				document.getElementById("Request").value = cleaned;
			}
			if (document.getElementById("SecondRequest").value == "" && document.getElementById("SecondRequest").style.display != "none") {
				let cleaned = y.share.map.get("requestSecond").replace(/['"]+/g, '')
				document.getElementById("SecondRequest").value = cleaned;
			}
			//if (event.name == "requestType") {
			//	setSelect("RequestType", y.share.map.get("requestType"));
			//}
			if (event.name == "database") {
				setSelect("DatabaseSelect", y.share.map.get("database"));
				toggleSecond();
			}
			if (event.name == "vizType") {
				//document.getElementById("Visualization").value = y.share.map.get("vizType");
				setSelect("Visualization", y.share.map.get("vizType"));
			}
		}
	});
	document.getElementById("queryForm").onsubmit = function (event) {
		if (document.getElementById("collabToggle").checked == true) {
			y.share.map.set("request", JSON.stringify(document.getElementById("Request").value));
			//y.share.map.set("requestType", JSON.stringify(document.getElementById("RequestType").value));
			y.share.map.set("database", JSON.stringify(document.getElementById("DatabaseSelect").value))
			y.share.map.set("vizType", JSON.stringify(document.getElementById("Visualization").value));
			if (document.getElementById("SecondRequest").style.display != "none") {
				y.share.map.set("requestSecond", JSON.stringify(document.getElementById("SecondRequest").value))
			}
		}
	};
});

function setSelect(id, input) {
	let select = document.getElementById(id);
	let temp  = input.replace(/['"]+/g, '');
	select.value = temp;
};

// Load the Visualization API and the corechart package.
google.charts.load('current', {'packages':['corechart']});
google.charts.load("current", {packages:["calendar"]});

function addDatabase() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (this.readyState == "4" && this.status == "200") {
			document.getElementById("Result").innerHTML = this.responseText;
		} else {
			document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
			document.getElementById("Result").innerHTML = this.responseText;
		}
	}
	var name = document.getElementById("DatabaseName0").value;
	var url = document.getElementById("DatabaseHost0").value;
	var dbSchema = document.getElementById("DatabaseSchema0").value;
	var user = document.getElementById("Username0").value;
	var password = document.getElementById("Password0").value;
	var dbType = document.getElementById("DatabaseType0").value;
	//var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql";
	var path = apiOptions.APIURL + "/graphql/graphql?input=";
	path = path + "mutation%7BaddDatabase"
	var object = "(name: \"" + name + "\", url:\"" + url + "\", dbSchema:\"" + dbSchema + "\", user:\"" + user + "\", password:\"" + password + "\", dbType:\"" + dbType + "\")%7D";
	object = object.replace(/\//g, "%2F");
	path = path + object
	//var object = "{" + "\"url\":\"" + url + "\", \"user\":\"" + user + "\", \"password\":\"" + password + "\"}";
	document.getElementById("Result").style.visibility = "visible";
	document.getElementById("Result").innerHTML = "running...";
	//path = "http://localhost:8080/MediabaseRESTAPI/rest/mediabase/database/list/anothertest2";
	request.open("GET", path , true);
	//request.setRequestHeader("Access-Control-Allow-Origin", "*");
	//request.setRequestHeader('Content-Type', "text/plain");  
	document.getElementById("QueryCheck").innerHTML = path;
	//request.send(object);
	request.send();
};

function executeRequest(query, visual, options, outputID) {
	if (false && document.getElementById("DatabaseSelect").value == "All") {
		var paths = [];
		var responses = [];
		//<var requestType = document.getElementById("RequestType").value;
		//var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql?input=";
		var path = apiOptions.APIURL + "/graphql/graphql?input=";
		//var input = document.getElementById("Request").value;
		var input = "query%7BdatabaseNames%7D";
		//var insertPath = path + requestType.toLowerCase() + "{mediabase_" + input + "}";
		var insertPath = path + input;
		// replace curly parentheses in accordance with RFC 1738
		insertPath = insertPath.replace(/{/g, "%7B");
		insertPath = insertPath.replace(/}/g, "%7D");
		paths.push(insertPath);
		
		input = document.getElementById("SecondRequest").value;
		//insertPath = path + requestType.toLowerCase() + "{mediabase_" + input + "}";
		// replace curly parentheses in accordance with RFC 1738
		insertPath = insertPath.replace(/{/g, "%7B");
		insertPath = insertPath.replace(/}/g, "%7D");
		paths.push(insertPath);
		console.log("Paths: " + paths);
		console.log("Paths length: " + paths.length);
		
		var request = new XMLHttpRequest();
		request.open("GET", paths[0], true);
		request.onreadystatechange = function() {
					if (this.readyState == "4" && this.status == "200") {
						responses.push(this.responseText);
						console.log("Response: " + this.responseText);
						if (responses.length > 1) {
							allQuery(responses, document.getElementById("Visualization").value);
						}
					}
			};
		request.send();
		
		request = new XMLHttpRequest();
		request.onreadystatechange = function() {
					if (this.readyState == "4" && this.status == "200") {
						responses.push(this.responseText);
						if (responses.length > 1) {
							allQuery(responses, document.getElementById("Visualization").value);
						}
					}
			};
		request.open("GET", paths[1], true);
		request.send();
		/*for (var i = 0; i < paths.length; i++) {
			(function(i) {
				console.log("Index: " + paths[i])
				let request = new XMLHttpRequest();
				request.open("GET", paths[i], true);
				request.onreadystatechange = function() {
					if (this.readyState == "4" && this.status == "200") {
						console.log("Response text: " + this.responseText);
						responses.push(this.responseText);
					}
			};
			request.send();
			})(i);
		}*/
	} else {
		var request = new XMLHttpRequest();
		request.onreadystatechange = function() {
			if (this.readyState == "4" && this.status == "200") {
				//document.getElementById("Request").style.borderColor = "black";
				document.getElementById("RequestLabel").style.color = "black";
				//document.getElementById("RequestLabel").innerHTML = "Visualization Request:";
				document.getElementById("Result").innerHTML = this.responseText;
				var data = this.responseText;
				getDatabases();
				//var chartType = document.getElementById("Visualization").value;
				//switch (chartType) {
				switch (visual) {
					case "GOOGLEBARCHART":
						createBaseChart(data, "GOOGLEBARCHART", options, outputID);
						break;
					case "GOOGLEPIECHART":
						createBaseChart(data, "GOOGLEPIECHART", options, outputID);
						break;
					case "GOOGLECOLUMNCHART":
						createBaseChart(data, "GOOGLECOLUMNCHART", options, outputID);
						break;
					case "GOOGLELINECHART":
						createLineChart(data, options, outputID);
						break;
					case "GOOGLEGEOCHART":
						createGeoChart(data, options, outputID);
						break;
					case "GOOGLECALENDARCHART":
						createCalendarChart(data, options, outputID);
						break;
					default:
						document.getElementById(outputID).value = data;
				}
				//document.getElementById("download").style.visibility = "visible";
				document.getElementById("KeyCheck").innerHTML = (new Date("2016-11-17 12:00:00.0")).getFullYear();
			} else if (this.status == "513"){
				document.getElementById("RequestLabel").style.color = "red";
				//document.getElementById("Request").style.borderColor = "red";
				document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
			} else {
				document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
			}
		}
	//var requestType = document.getElementById("RequestType").value;
	//var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql?input=";
	//var path = "http://137.226.58.233:8080/graphql/graphql?input=";
	var path = apiOptions.APIURL + "/graphql/graphql?input="
	//var input = document.getElementById("Request").value;
	//var database = document.getElementById("DatabaseSelect").value;
	//path = path + requestType.toLowerCase() + "{" + database + "_" + input + "}";
	path = path + query;
	// replace curly parentheses in accordance with RFC 1738
	path = path.replace(/{/g, "%7B");
	path = path.replace(/}/g, "%7D");
	document.getElementById("Result").style.visibility = "visible";
	document.getElementById("Result").innerHTML = path;
	request.open("GET", path, true);
	document.getElementById("Result").innerHTML = "still running...";
	request.send();
	
	// debugging
	document.getElementById("Result").innerHTML = "further running...";
	document.getElementById("QueryCheck").innerHTML = path;

	//document.getElementById("download").style.visibility = "visible";
	}
};

function allQuery(data, chart) {
	console.log("Data: " + data);
	console.log("Chart: " + chart);
	var combinedData = data[0];
	if (data.length > 1) {
		combinedData = data[0].slice(0, data[0].length - 2);
	} else {
		combinedData = data[0];
	}
	for (var i = 1; i < data.length; i++) {
		let index = data[i].indexOf(":");
		combinedData = combinedData + "," + data[i].slice(index + 2);
	}
	console.log("combinedData: " + combinedData);
	switch (chart) {
		case "GOOGLEBARCHART":
			createBaseChart(combinedData, "GOOGLEBARCHART");
			break;
		case "GOOGLEPIECHART":
			createBaseChart(combinedData, "GOOGLEPIECHART");
			break;
		case "GOOGLECOLUMNCHART":
			createBaseChart(combinedData, "GOOGLECOLUMNCHART");
			break;
		case "GOOGLELINECHART":
			createLineChart(combinedData);
			break;
		case "GOOGLEGEOCHART":
			createGeoChart(combinedData);
			break;
		case "GOOGLECALENDARCHART":
			createCalendarChart(combinedData);
			break;
		default:
			break;
		}
};

// collect input parameter for GraphQL request for direct request
function callRequest() {
	var query = document.getElementById("Request").value;
	var visual = document.getElementById("Visualization").value;
	var outputID = "chartDiv";
	if (document.getElementById("ChartOptions").value != "") {
		var options = JSON.parse(document.getElementById("ChartOptions").value)
	} else {
		var options = {"title":"with default options","width":400,"height":400};
	}
	
	executeRequest(query, visual, options, outputID);
};

// collect input parameter for GraphQL request for request from request construction
function callRequestConstruction() {
	if (document.getElementById("DatabaseSelect").value == "All") {
		if (document.getElementById("TemplateType").value == "Media-User") {
			var query = "query{all_reviews(id:\"" +  document.getElementById("userInput").value + "\"){id, rating}}";
		} else if (document.getElementById("TemplateType").value == "Media-Platform") {
			var query = "query{all_reviews{author_id, perma_link}}";
		}
	} else {
		if (document.getElementById("TemplateType").value == "Media-User") {
			var query = "query{" + document.getElementById("DatabaseSelect").value + "_bw_entries(id:\"" + 
			document.getElementById("userInput").value + "\"){id, mood}}";
		} else if (document.getElementById("TemplateType").value == "Media-Platform") {
			var query = "query{" + document.getElementById("DatabaseSelect").value + "_bw_entries{author_id, perma_link}}";
		}
	}
	
	var visual = document.getElementById("VisualizationConstructor").value;
	var outputID = "chartDivConstructor";
	if (document.getElementById("ChartOptionsConstructor").value != "") {
		var options = JSON.parse(document.getElementById("ChartOptionsConstructor").value)
	} else {
		var options = {"title":"with default options","width":400,"height":400};
	}
	executeRequest(query, visual, options, outputID);
}

function createGeoChart(data, options, outputID) {
	var output = "{\"bw_author\":[{\"authorname\":\"Germany\", \"id\":\"49\"}, " +
	"{\"authorname\":\"France\", \"id\":\"50\"}," +
	"{ \"authorname\":\"United States\", \"id\":\"51\"}," +
	"{ \"authorname\":\"China\", \"id\":\"52\"}," +
	"{ \"authorname\":\"Japan\",\"id\":\"53\"}," +
	"{ \"authorname\":\"Sweden\",\"id\":\"54\"}," +
	"{ \"authorname\":\"South Africa\",\"id\":\"63\"}," +
	"{ \"authorname\":\"Brazil\",\"id\":\"62\"}]}";
	
	var array = JSON.parse(output);
	var key = Object.keys(array);
	var formattedArray = formattingBarChart(array[key[0]]);
	document.getElementById("ChartCheck").innerHTML = JSON.stringify(formattedArray);
	// Create the data table.
	var data = new google.visualization.DataTable(formattedArray);

	// Set chart options
	//var options = {'title':'Query Visualization',
	//			   'width':400,
	//			   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.GeoChart(document.getElementById(outputID));
	if (outputID == "chartDiv") {
		var chartImageDiv = document.getElementById('chartImageDiv');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClick").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	} else if(outputID == "chartDivConstructor") {
		var chartImageDiv = document.getElementById('chartImageDivConstructor');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClickConstructor").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	}
	
	chart.draw(data, options);
	//chart.draw(data, options);
	//google.visualization.events.addListener(chart, 'ready', function () {
        //chartDiv.innerHTML =  '<img src="' + chart.getImageURI() + '">';
        //console.log(chartDiv.innerHTML);
      	//});
	
};

function createLineChart(data, options, ouputID) {
	var output = "{\"bw_author\":[{\"authorname\":\"Test 0\", \"id\":\"49\", \"id2\":\"19\"}, " +
	"{\"authorname\":\"Test 1\", \"id\":\"50\", \"id2\":\"29\"}," +
	"{ \"authorname\":\"Test 2\", \"id\":\"51\", \"id2\":\"39\"}," +
	"{ \"authorname\":\"Test 3\", \"id\":\"52\", \"id2\":\"49\"}," +
	"{ \"authorname\":\"Test 4\",\"id\":\"53\", \"id2\":\"59\"}," +
	"{ \"authorname\":\"Test 5\",\"id\":\"54\", \"id2\":\"69\"}," +
	"{ \"authorname\":\"Test 6\",\"id\":\"63\", \"id2\":\"79\"}," +
	"{ \"authorname\":\"Test 7\",\"id\":\"62\", \"id2\":\"89\"}]}";
	
	output = data;
	
	var array = JSON.parse(output);
	var key = Object.keys(array);
	var formattedArray = formattingBarChart(array[key[0]]);
	document.getElementById("ChartCheck").innerHTML = JSON.stringify(formattedArray);

	// Create the data table.
	var data = new google.visualization.DataTable(formattedArray);

	// Set chart options
	//var options = {'title':'Query Visualization',
	//			   'width':400,
	//			   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.LineChart(document.getElementById(outputID));
	if (outputID == "chartDiv") {
		var chartImageDiv = document.getElementById('chartImageDiv');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClick").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	} else if(outputID == "chartDivConstructor") {
		var chartImageDiv = document.getElementById('chartImageDivConstructor');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClickConstructor").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	}
	chart.draw(data, options);
};

function createBaseChart(data, type, options, outputID) {
	var output = "{\"bw_author\":[{\"authorname\":\"Test 0\", \"id\":\"49\"}, " +
	"{\"authorname\":\"Test 1\", \"id\":\"50\"}," +
	"{ \"authorname\":\"Test 2\", \"id\":\"51\"}," +
	"{ \"authorname\":\"Test 3\", \"id\":\"52\"}," +
	"{ \"authorname\":\"Test 4\",\"id\":\"53\"}," +
	"{ \"authorname\":\"Test 5\",\"id\":\"54\"}," +
	"{ \"authorname\":\"Test 6\",\"id\":\"63\"}," +
	"{ \"authorname\":\"Test 7\",\"id\":\"62\"}]}";
	
	output = data;
	var array = JSON.parse(output);
	var key = Object.keys(array);
	var formattedArray = formattingBarChart(array[key[0]]);
	document.getElementById("ChartCheck").innerHTML = JSON.stringify(formattedArray);

	// Create the data table.
	var data = new google.visualization.DataTable(formattedArray);

	// Set chart options
	//var options = {'title':'Query Visualization',
	//			   'width':400,
	//			   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart;
	if (type == "GOOGLEBARCHART") {
		chart = new google.visualization.BarChart(document.getElementById(outputID));
		//var chartImageDiv = document.getElementById('chartImageDiv');
		//google.visualization.events.addListener(chart, 'ready', function () {
        //chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		//document.getElementById("downloadClick").style.display = "block";
        //console.log(chartImageDiv.innerHTML);
		//});
	}
	else if (type == "GOOGLEPIECHART") {
		chart = new google.visualization.PieChart(document.getElementById(outputID));
		//var chartImageDiv = document.getElementById('chartImageDiv');
		//google.visualization.events.addListener(chart, 'ready', function () {
        //chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		//document.getElementById("downloadClick").style.display = "block";
        //console.log(chartImageDiv.innerHTML);
		//});

	}
	else {
		chart = new google.visualization.ColumnChart(document.getElementById(outputID));
		//var chartImageDiv = document.getElementById('chartImageDiv');
		//google.visualization.events.addListener(chart, 'ready', function () {
        //chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		//document.getElementById("downloadClick").style.display = "block";
        //console.log(chartImageDiv.innerHTML);
		//});
	}
	
	if (outputID == "chartDiv") {
		var chartImageDiv = document.getElementById('chartImageDiv');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClick").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	} else if(outputID == "chartDivConstructor") {
		var chartImageDiv = document.getElementById('chartImageDivConstructor');
		google.visualization.events.addListener(chart, 'ready', function () {
		chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
		document.getElementById("downloadClickConstructor").style.display = "block";
		console.log(chartImageDiv.innerHTML);
		document.getElementById(outputID).style.visibility = "visible";
		});
	}
	chart.draw(data, options);
};

function createCalendarChart(data, options, outputID) {
	var array = JSON.parse(data);
	var key = Object.keys(array);
	var formattedArray = formattingCalendarChart(array[key[0]]);
	document.getElementById("ChartCheck").innerHTML = JSON.stringify(formattedArray);

	// Create the data table.
	var data = new google.visualization.DataTable(formattedArray);

	// Set chart options
	//var options = {'title':'testing','width':1000,'height':1000};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.Calendar(document.getElementById(outputID));
	chart.draw(data, options);
};

function formattingCalendarChart(json) {
	var formattedJSON = "{";
	var firstElem = json[0];
	var keys = Object.keys(firstElem);
	var cols = "\"cols\":[";
	var type = "";
	for (var i = 0; i < keys.length; i++) {
		type = typeof firstElem[keys[i]];
		if (i == 0) {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"" + "date" + "\"" + "}";
		} else {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"number\"" + "}";
		}
		if (i < (keys.length - 1)) {
			cols = cols + ",";
		}
	}
	cols = cols + "],";
	var rows = "\"rows\":[";
	count = 0;
	var keyCount = 0;
	for (var elem of json) {
		rows = rows + "{\"c\":[";
		keyCount = 0;
		for (var key of keys) {
			if (typeof elem[key] == "number" || keyCount >= 1) {
				//document.getElementById("KeyCheck").innerHTML = typeof elem[key];
				rows = rows + "{\"v\":" + elem[key] + "}";
			} else {
				testing = 
				rows = rows + "{\"v\":\"Date(" + formatDate(elem[key]) + ")\"}";
			}
			if (!(keyCount >= (keys.length - 1))) {
				rows = rows + ",";
			}
			keyCount = keyCount + 1;
		}
		rows = rows + "]}";
		if (!(count >= (json.length - 1))) {
			rows = rows + ",";
		}
		count = count + 1;
		//rows = rows + ",";
	}
	rows = rows + "]";
	formattedJSON = formattedJSON + cols + rows;
	formattedJSON = formattedJSON + "}";
	return formattedJSON;
};

// format json in accordance to google bar chart, column chart, and pie chart requirements
function formattingBarChart(json) {
	var formattedJSON = "{";
	var firstElem = json[0];
	var keys = Object.keys(firstElem);
	var cols = "\"cols\":[";
	var type = "";
	for (var i = 0; i < keys.length; i++) {
		type = typeof firstElem[keys[i]];
		if (i == 0) {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"" + type + "\"" + "}";
		} else {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"number\"" + "}";
		}
		if (i < (keys.length - 1)) {
			cols = cols + ",";
		}
	}
	cols = cols + "],";
	var rows = "\"rows\":[";
	count = 0;
	var keyCount = 0;
	for (var elem of json) {
		rows = rows + "{\"c\":[";
		keyCount = 0;
		for (var key of keys) {
			if (typeof elem[key] == "number" || keyCount >= 1) {
				document.getElementById("KeyCheck").innerHTML = typeof elem[key];
				rows = rows + "{\"v\":" + elem[key] + "}";
			} else {
				rows = rows + "{\"v\":\"" + elem[key] + "\"}";
			}
			if (!(keyCount >= (keys.length - 1))) {
				rows = rows + ",";
			}
			keyCount = keyCount + 1;
		}
		rows = rows + "]}";
		if (!(count >= (json.length - 1))) {
			rows = rows + ",";
		}
		count = count + 1;
		//rows = rows + ",";
	}
	rows = rows + "]";
	formattedJSON = formattedJSON + cols + rows;
	formattedJSON = formattedJSON + "}";
	return formattedJSON;
};

// format json in accordance to google line chart requirements
function formattingLineChart(json) {
	
	var formattedJSON = "{";
	var firstElem = json[0];
	var keys = Object.keys(firstElem);
	var cols = "\"cols\":[";
	var type = "";
	for (var i = 0; i < keys.length; i++) {
		type = typeof firstElem[keys[i]];
		if (i == 0) {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"" + type + "\"" + "}";
		} else {
			cols = cols + "{\"label\": \"" + keys[i] + "\", \"type\": \"number\"" + "}";
		}
		if (i < (keys.length - 1)) {
			cols = cols + ",";
		}
	}
	cols = cols + "],";
	var rows = "\"rows\":[";
	count = 0;
	var keyCount = 0;
	for (var elem of json) {
		rows = rows + "{\"c\":[";
		keyCount = 0;
		for (var key of keys) {
			if (typeof elem[key] == "number" || keyCount >= 1) {
				document.getElementById("KeyCheck").innerHTML = typeof elem[key];
				rows = rows + "{\"v\":" + elem[key] + "}";
			} else {
				rows = rows + "{\"v\":\"" + elem[key] + "\"}";
			}
			if (!(keyCount >= (keys.length - 1))) {
				rows = rows + ",";
			}
			keyCount = keyCount + 1;
		}
		rows = rows + "]}";
		if (!(count >= (json.length - 1))) {
			rows = rows + ",";
		}
		count = count + 1;
		//rows = rows + ",";
	}
	rows = rows + "]";
	formattedJSON = formattedJSON + cols + rows;
	formattedJSON = formattedJSON + "}";
	return formattedJSON;
};

// format date to yyyy, mm, dd format, dictated by Google Charts API
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) 
        month = '0' + month;
    if (day.length < 2) 
        day = '0' + day;

    return [year, month, day].join(', ');
};

function getDatabases() {
	var request = new XMLHttpRequest();
		request.onreadystatechange = function() {
			if (this.readyState == "4" && this.status == "200") {
				document.getElementById("Result").innerHTML = this.responseText;
				var data = this.responseText;
				data = (JSON.parse(data).databaseNames).split(", ");
				for (var i = 0; i < data.length; i++) {
					var selection = document.getElementById("DatabaseSelect");
					var opt = document.createElement("option");
					var name = "";
					if (i == 0) {
						name = data[i].substring(1, data[i].length);
					} else if (i == data.length - 1) {
						name = data[i].substring(0, data[i].length - 1);
					} else {
						name = data[i];
					}
					var present = false;
					for (var j = 0; j < document.getElementById("DatabaseSelect").options.length; j++) {
						if (document.getElementById("DatabaseSelect").options[j].value == name) {
							present = true;
						}
					}
					if (!present) {
						opt.value = name;
						opt.innerHTML = name;
						selection.appendChild(opt);
					}
				}
			} else {
				document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
			}
		}
	var path = apiOptions.APIURL + "/graphql/graphql?input="
	path = path + "query%7BdatabaseNames%7D";
	// replace curly parentheses in accordance with RFC 1738
	path = path.replace(/{/g, "%7B");
	path = path.replace(/}/g, "%7D");
	document.getElementById("Result").style.visibility = "visible";
	document.getElementById("Result").innerHTML = path;
	request.open("GET", path, true);
	document.getElementById("Result").innerHTML = "still running...";
	console.log("Path: " + path);
	request.send();
};

function setTargetDatabases() {
	var databases = getDatabases();
	console.log("Databases: " + databases);
	for (var i = 0; i < databases.length; i++) {
		var selection = document.getElementById("DatabaseSelect");
		var opt = document.createElement("option");
		if (i == 0) {
			opt.value = databases[i].substring(1, databases[i].length - 1);
			opt.innerHTML = databases[i].substring(1, databases[i].length - 1);
		} else if (i == databases.length - 1) {	
			opt.value = databases[i].substring(0, databases[i].length - 2);
			opt.innerHTML = databases[i].substring(0, databases[i].length - 2);
		} else {
			opt.value = databases[i];
			opt.innerHTML = databases[i];
		}
		selection.appendChild(opt);
	}
};

function loadFile() {
	console.log("loadFile");
	var result = null;
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
			if (this.readyState == "4" && this.status == "200") {
				result = xmlhttp.responseText;
				buildReport(result);
			}
		}
	xmlhttp.open("GET", "../../savedQueries.json", false);
	xmlhttp.send();
};


// load queries from .json file
function loadQueries(event) {
	console.log("load Queries");
	var input = event.target;
	var reader = new FileReader();
	reader.onload = function() {
		buildReport(reader.result);
		//var count = 0;
		//while(document.getElementById("displayChart" + count) != null){
		//	document.getElementById("displayChart" + count).parentNode.removeChild(document.getElementById("displayChart" + count));
		//	count++;
		//}
		//var queries = reader.result;
		//var json = JSON.parse(reader.result);
		//var array = json.queries;
		//console.log("setTest2 " + apiOptions.setTest);
		//for (var i = 0; i < array.length; i++) {
			//console.log("Title: " + array[i].options.title);
			//var para = document.createElement("p");
			//var node = document.createTextNode(array[i].query);
			//para.appendChild(node);
			//var element = document.getElementById("reportView");
			//element.appendChild(para);
			//var chartVisual = document.createElement("div");
			//chartVisual.setAttribute("id", "displayChart" + i);
			//element.appendChild(chartVisual);
			//executeRequest(array[i].query, array[i].visual, array[i].options, "displayChart" + i);
		//}
	};
	reader.readAsText(input.files[0]);
};

function buildReport(input) {
	var count = 0;
	console.log("Building Resport");
	while(document.getElementById("displayChart" + count) != null){
		console.log("Building...");
		document.getElementById("displayChart" + count).parentNode.removeChild(document.getElementById("displayChart" + count));
		count++;
	}
	//var queries = input;
	var json = JSON.parse(input);
	var array = json.queries;
	for (var i = 0; i < array.length; i++) {
		console.log("Title: " + array[i].options.title);
		//var para = document.createElement("p");
		//var node = document.createTextNode(array[i].query);
		//para.appendChild(node);
		var element = document.getElementById("reportView");
		//element.appendChild(para);
		var chartVisual = document.createElement("div");
		chartVisual.setAttribute("id", "displayChart" + i);
		element.appendChild(chartVisual);
		executeRequest(array[i].query, array[i].visual, array[i].options, "displayChart" + i);
	}
};

function setAPI(url) {
	apiOptions.APIURL = url;
};