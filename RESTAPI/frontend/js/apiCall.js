

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
	y.share.map.delete("requestType");
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
			if (event.name == "requestType") {
				setSelect("RequestType", y.share.map.get("requestType"));
			}
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
			y.share.map.set("requestType", JSON.stringify(document.getElementById("RequestType").value));
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
	var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql";
	path = path + "?input=mutation%7BaddDatabase"
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
function query() {
	if (document.getElementById("DatabaseSelect").value == "All") {
		var paths = [];
		var responses = [];
		var requestType = document.getElementById("RequestType").value;
		var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql?input=";
		var input = document.getElementById("Request").value;
		var insertPath = path + requestType.toLowerCase() + "{mediabase_" + input + "}";
		// replace curly parentheses in accordance with RFC 1738
		insertPath = insertPath.replace(/{/g, "%7B");
		insertPath = insertPath.replace(/}/g, "%7D");
		paths.push(insertPath);
		
		input = document.getElementById("SecondRequest").value;
		insertPath = path + requestType.toLowerCase() + "{mediabase_" + input + "}";
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
				document.getElementById("Result").innerHTML = this.responseText;
				var data = this.responseText;
				var chartType = document.getElementById("Visualization").value;
				switch (chartType) {
					case "GOOGLEBARCHART":
						createBaseChart(data, "GOOGLEBARCHART");
						break;
					case "GOOGLEPIECHART":
						createBaseChart(data, "GOOGLEPIECHART");
						break;
					case "GOOGLECOLUMNCHART":
						createBaseChart(data, "GOOGLECOLUMNCHART");
						break;
					case "GOOGLELINECHART":
						createLineChart(data);
						break;
					case "GOOGLEGEOCHART":
						createGeoChart(data);
						break;
					case "GOOGLECALENDARCHART":
						createCalendarChart(data);
						break;
					default:
			}
				document.getElementById("download").style.visibility = "visible";
				document.getElementById("KeyCheck").innerHTML = (new Date("2016-11-17 12:00:00.0")).getFullYear();
			} else {
				document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
			}
		}
	var requestType = document.getElementById("RequestType").value;
	var path = "http://localhost:9000/GraphqlAPITest/graphql/graphqlrest/graphql?input=";
	var input = document.getElementById("Request").value;
	var database = document.getElementById("DatabaseSelect").value;
	path = path + requestType.toLowerCase() + "{" + database + "_" + input + "}";
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

	document.getElementById("download").style.visibility = "visible";
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
function createGeoChart(data) {
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
	var options = {'title':'Query Visualization',
				   'width':400,
				   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.GeoChart(document.getElementById('chartDiv'));
	document.getElementById('chartDiv').style.visibility = "visible";
	chart.draw(data, options);
	//chart.draw(data, options);
	//google.visualization.events.addListener(chart, 'ready', function () {
        //chartDiv.innerHTML =  '<img src="' + chart.getImageURI() + '">';
        //console.log(chartDiv.innerHTML);
      	//});
	
};

function createLineChart(data) {
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
	var options = {'title':'Query Visualization',
				   'width':400,
				   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.LineChart(document.getElementById('chartDiv'));
	chart.draw(data, options);
};

function createBaseChart(data, type) {
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
	var options = {'title':'Query Visualization',
				   'width':400,
				   'height':300};

	// Instantiate and draw our chart, passing in some options.
	var chart;
	if (type == "GOOGLEBARCHART") {
		chart = new google.visualization.BarChart(document.getElementById('chartDiv'));
	}
	else if (type == "GOOGLEPIECHART") {
		chart = new google.visualization.PieChart(document.getElementById('chartDiv'));
	}
	else {
		chart = new google.visualization.ColumnChart(document.getElementById('chartDiv'));
	}
	chart.draw(data, options);
};

function createCalendarChart(data) {
	var array = JSON.parse(data);
	var key = Object.keys(array);
	var formattedArray = formattingCalendarChart(array[key[0]]);
	document.getElementById("ChartCheck").innerHTML = JSON.stringify(formattedArray);

	// Create the data table.
	var data = new google.visualization.DataTable(formattedArray);

	// Set chart options
	var options = {'title':'Query Visualization',
				   'width':1000,
				   'height':1000};

	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.Calendar(document.getElementById('chartDiv'));
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
}