// sets element with id to input
function setSelect(id, input) {
  let select = document.getElementById(id);
  let temp = input.replace(/['"]+/g, '');
  select.value = temp;
}

// Load the Visualization API and the corechart package.
google.charts.load('current', { packages: ['corechart'] });
google.charts.load('current', { packages: ['calendar'] });

// adds database by sending GraphQL request
function addDatabase() {
  document.getElementById('databaseNameLabel').style.color = 'black';
  var request = new XMLHttpRequest();
  request.onreadystatechange = function () {
    if (this.readyState == '4' && this.status == '200') {
      console.log('Response:' + this.responseText);
      document.getElementById('databaseNameLabel').style.color = 'green';
    } else if (this.readyState == '1') {
      document.getElementById('databaseNameLabel').style.color = 'blue';
    } else {
      console.log('Response:' + this.responseText);
      console.log('readyState: ' + this.readyState);
      console.log('status: ' + this.status);
      document.getElementById('databaseNameLabel').style.color = 'red';
    }
  };
  var name = document.getElementById('DatabaseName0').value;
  var url = document.getElementById('DatabaseHost0').value;
  var dbSchema = document.getElementById('DatabaseSchema0').value;
  var user = document.getElementById('Username0').value;
  var password = document.getElementById('Password0').value;
  var dbType = document.getElementById('DatabaseType0').value;
  var path = apiOptions.APIURL + '/graphql/graphql?query=';
  path = path + 'mutation%7BaddDatabase';
  var object =
    '(name:%22' +
    name +
    '%22, url:%22' +
    url +
    '%22,dbSchema:%22' +
    dbSchema +
    '%22,user:%22' +
    user +
    '%22,password:%22' +
    password +
    '%22,dbType:%22' +
    dbType +
    '%22)%7D';
  object = object.replace(/\//g, '%2F');
  path = path + object;
  path = path.replace(/{/g, '%7B');
  path = path.replace(/}/g, '%7D');

  document.getElementById('Result').style.visibility = 'visible';
  document.getElementById('Result').innerHTML = 'running...';
  request.open('GET', path, true);
  document.getElementById('QueryCheck').innerHTML = path;
  request.send();
}

// executes GraphQL request in section request and template
function executeRequest(query, visual, options, outputID) {
  if (false && document.getElementById('DatabaseSelect').value == 'All') {
    var paths = [];
    var responses = [];
    var path = apiOptions.APIURL + '/graphql/graphql?query=';
    var input = 'query%7BdatabaseNames%7D';
    var insertPath = path + input;
    // replace curly parentheses in accordance with RFC 1738
    insertPath = insertPath.replace(/{/g, '%7B');
    insertPath = insertPath.replace(/}/g, '%7D');
    paths.push(insertPath);

    input = document.getElementById('SecondRequest').value;
    // replace curly parentheses in accordance with RFC 1738
    insertPath = insertPath.replace(/{/g, '%7B');
    insertPath = insertPath.replace(/}/g, '%7D');
    paths.push(insertPath);
    console.log('Paths: ' + paths);
    console.log('Paths length: ' + paths.length);

    var request = new XMLHttpRequest();
    request.open('GET', paths[0], true);
    request.onreadystatechange = function () {
      if (this.readyState == '4' && this.status == '200') {
        responses.push(this.responseText);
        console.log('Response: ' + this.responseText);
        if (responses.length > 1) {
          allQuery(responses, document.getElementById('Visualization').value);
        }
      }
    };
    request.send();

    request = new XMLHttpRequest();
    request.onreadystatechange = function () {
      if (this.readyState == '4' && this.status == '200') {
        responses.push(this.responseText);
        if (responses.length > 1) {
          allQuery(responses, document.getElementById('Visualization').value);
        }
      }
    };
    request.open('GET', paths[1], true);
    request.send();
  } else {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
      if (this.readyState == '4' && this.status == '200') {
        document.getElementById('RequestLabel').style.color = 'black';
        document.getElementById('Result').innerHTML = this.responseText;
        console.log('Result of request: ' + this.responseText);
        var data = this.responseText;
        getDatabases();
        switch (visual) {
          case 'GOOGLEBARCHART':
            createBaseChart(data, 'GOOGLEBARCHART', options, outputID);
            break;
          case 'GOOGLEPIECHART':
            createBaseChart(data, 'GOOGLEPIECHART', options, outputID);
            break;
          case 'GOOGLECOLUMNCHART':
            createBaseChart(data, 'GOOGLECOLUMNCHART', options, outputID);
            break;
          case 'GOOGLELINECHART':
            createLineChart(data, options, outputID);
            break;
          case 'GOOGLEGEOCHART':
            createGeoChart(data, options, outputID);
            break;
          case 'GOOGLECALENDARCHART':
            createCalendarChart(data, options, outputID);
            break;
          default:
            document.getElementById(outputID).value = data;
        }
        document.getElementById('KeyCheck').innerHTML = new Date(
          '2016-11-17 12:00:00.0'
        ).getFullYear();
      } else if (this.status == '513') {
        document.getElementById('RequestLabel').style.color = 'red';
        document.getElementById('Result').innerHTML =
          'Status: ' + this.status + ' readyState: ' + this.readyState;
      } else {
        document.getElementById('Result').innerHTML =
          'Status: ' + this.status + ' readyState: ' + this.readyState;
      }
    };

    var path = apiOptions.APIURL + '/graphql/graphql?query=';
    path = path + query;
    // replace curly parentheses in accordance with RFC 1738
    path = path.replace(/{/g, '%7B');
    path = path.replace(/}/g, '%7D');
    document.getElementById('Result').style.visibility = 'visible';
    document.getElementById('Result').innerHTML = path;
    console.log('Path: ' + path);
    request.open('GET', path, true);
    document.getElementById('Result').innerHTML = 'still running...';
    request.send();

    // debugging
    document.getElementById('Result').innerHTML = 'further running...';
    document.getElementById('QueryCheck').innerHTML = path;
  }
}

// prepares data for Google Charts
function allQuery(data, chart) {
  console.log('Data: ' + data);
  console.log('Chart: ' + chart);
  var combinedData = data[0];
  if (data.length > 1) {
    combinedData = data[0].slice(0, data[0].length - 2);
  } else {
    combinedData = data[0];
  }
  for (var i = 1; i < data.length; i++) {
    let index = data[i].indexOf(':');
    combinedData = combinedData + ',' + data[i].slice(index + 2);
  }
  console.log('combinedData: ' + combinedData);
  switch (chart) {
    case 'GOOGLEBARCHART':
      createBaseChart(combinedData, 'GOOGLEBARCHART');
      break;
    case 'GOOGLEPIECHART':
      createBaseChart(combinedData, 'GOOGLEPIECHART');
      break;
    case 'GOOGLECOLUMNCHART':
      createBaseChart(combinedData, 'GOOGLECOLUMNCHART');
      break;
    case 'GOOGLELINECHART':
      createLineChart(combinedData);
      break;
    case 'GOOGLEGEOCHART':
      createGeoChart(combinedData);
      break;
    case 'GOOGLECALENDARCHART':
      createCalendarChart(combinedData);
      break;
    default:
      break;
  }
}

// collect input parameter for GraphQL request for direct request
function callRequest() {
  var query = document.getElementById('Request').value;
  var visual = document.getElementById('Visualization').value;
  var outputID = 'chartDiv';
  if (document.getElementById('ChartOptions').value != '') {
    var options = JSON.parse(document.getElementById('ChartOptions').value);
  } else {
    var options = { title: 'with default options', width: 400, height: 400 };
  }

  executeRequest(query, visual, options, outputID);
}

// collect input parameter for GraphQL request for request from request construction
function callRequestConstruction() {
  if (document.getElementById('DatabaseSelect').value == 'All') {
    if (document.getElementById('TemplateType').value == 'Media-User') {
      var query =
        'query{all_reviews(id:"' + document.getElementById('userInput').value + '"){date, mood}}';
    } else if (document.getElementById('TemplateType').value == 'Media-Platform') {
      var query = 'query{all_reviews{author_id, perma_link}}';
    }
  } else {
    if (document.getElementById('TemplateType').value == 'Media-User') {
      if (document.getElementById('DatabaseSelect').value == 'las2peer') {
        var query =
          'query{' +
          document.getElementById('DatabaseSelect').value +
          '_reviewentry(id:"' +
          document.getElementById('userInput').value +
          '"){author_id, mood}}';
        query =
          'query{' +
          document.getElementById('DatabaseSelect').value +
          '_reviewentry{author_id, mood}}';
      } else {
        var query =
          'query{' +
          document.getElementById('DatabaseSelect').value +
          '_bw_entries(id:"' +
          document.getElementById('userInput').value +
          '"){id, mood}}';
      }
    } else if (document.getElementById('TemplateType').value == 'Media-Platform') {
      var query =
        'query{' +
        document.getElementById('DatabaseSelect').value +
        '_bw_entries{author_id, perma_link}}';
    }
  }

  var visual = document.getElementById('VisualizationConstructor').value;
  var outputID = 'chartDivConstructor';
  if (document.getElementById('ChartOptionsConstructor').value != '') {
    var options = JSON.parse(document.getElementById('ChartOptionsConstructor').value);
  } else {
    var options = { title: 'with default options', width: 400, height: 400 };
  }
  executeRequest(query, visual, options, outputID);
}

// creates Google Geo Chart of data with option in <p> with id outputID
function createGeoChart(data, options, outputID) {
  var array = JSON.parse(data);
  var key = Object.keys(array);
  var formattedArray = formattingGeoChart(array[key[0]]);
  document.getElementById('ChartCheck').innerHTML = JSON.stringify(formattedArray);
  // Create the data table.
  var data = new google.visualization.DataTable(formattedArray);

  // Instantiate and draw our chart, passing in some options.
  var chart = new google.visualization.GeoChart(document.getElementById(outputID));
  if (outputID == 'chartDiv') {
    var chartImageDiv = document.getElementById('chartImageDiv');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClick').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  } else if (outputID == 'chartDivConstructor') {
    var chartImageDiv = document.getElementById('chartImageDivConstructor');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClickConstructor').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  }

  chart.draw(data, options);
}

// creates Google Line Chart of data with option in <p> with id outputID
function createLineChart(data, options, outputID) {
  var output = data;

  var array = JSON.parse(output);
  var key = Object.keys(array);
  var formattedArray = formattingBarChart(array[key[0]]);
  document.getElementById('ChartCheck').innerHTML = JSON.stringify(formattedArray);

  // Create the data table.
  var data = new google.visualization.DataTable(formattedArray);

  // Instantiate and draw our chart, passing in some options.
  var chart = new google.visualization.LineChart(document.getElementById(outputID));
  if (outputID == 'chartDiv') {
    var chartImageDiv = document.getElementById('chartImageDiv');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClick').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  } else if (outputID == 'chartDivConstructor') {
    var chartImageDiv = document.getElementById('chartImageDivConstructor');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClickConstructor').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  }
  chart.draw(data, options);
}

// creates Google Column, Pie or Bar Chart of data with option in <p> with id outputID
function createBaseChart(data, type, options, outputID) {
  var output = data;
  var array = JSON.parse(output);
  var key = Object.keys(array);
  var formattedArray = formattingBarChart(array[key[0]]);
  document.getElementById('ChartCheck').innerHTML = JSON.stringify(formattedArray);

  // Create the data table.
  var data = new google.visualization.DataTable(formattedArray);

  // Instantiate and draw our chart, passing in some options.
  var chart;
  if (type == 'GOOGLEBARCHART') {
    chart = new google.visualization.BarChart(document.getElementById(outputID));
  } else if (type == 'GOOGLEPIECHART') {
    chart = new google.visualization.PieChart(document.getElementById(outputID));
  } else {
    chart = new google.visualization.ColumnChart(document.getElementById(outputID));
  }

  if (outputID == 'chartDiv') {
    var chartImageDiv = document.getElementById('chartImageDiv');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClick').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  } else if (outputID == 'chartDivConstructor') {
    var chartImageDiv = document.getElementById('chartImageDivConstructor');
    google.visualization.events.addListener(chart, 'ready', function () {
      chartImageDiv.innerHTML = '<img src="' + chart.getImageURI() + '">';
      document.getElementById('downloadClickConstructor').style.display = 'block';
      console.log(chartImageDiv.innerHTML);
      document.getElementById(outputID).style.visibility = 'visible';
    });
  }
  chart.draw(data, options);
}

// creates Google Calendar Chart of data with option in <p> with id outputID
function createCalendarChart(data, options, outputID) {
  var array = JSON.parse(data);
  var key = Object.keys(array);
  var formattedArray = formattingCalendarChart(array[key[0]]);
  document.getElementById('ChartCheck').innerHTML = JSON.stringify(formattedArray);

  // Create the data table.
  var data = new google.visualization.DataTable(formattedArray);

  // Instantiate and draw our chart, passing in some options.
  var chart = new google.visualization.Calendar(document.getElementById(outputID));
  chart.draw(data, options);
}

// format json in accordance to google geo chart requirements
function formattingGeoChart(json) {
  var formattedJSON = '{';
  var firstElem = json[0];
  var keys = Object.keys(firstElem);
  var cols = '"cols":[';
  var type = '';
  for (var i = 0; i < keys.length; i++) {
    type = typeof firstElem[keys[i]];
    if (i == 0) {
      cols = cols + '{"label": "country", "type": "string"' + '}';
    } else {
      cols = cols + '{"label": "count", "type": "number"' + '}';
    }
    if (i < keys.length - 1) {
      cols = cols + ',';
    }
  }
  cols = cols + '],';
  var rows = '"rows":[';
  count = 0;
  var keyCount = 0;
  for (var elem of json) {
    rows = rows + '{"c":[';
    keyCount = 0;
    rows = rows + '{"v":"' + elem['country'] + '"},';
    rows = rows + '{"v":' + parseInt(elem['count'], 10) + '}';
    rows = rows + ']}';
    if (!(count >= json.length - 1)) {
      rows = rows + ',';
    }
    count = count + 1;
    //rows = rows + ",";
  }
  rows = rows + ']';
  formattedJSON = formattedJSON + cols + rows;
  formattedJSON = formattedJSON + '}';
  console.log('formatted: ' + formattedJSON);
  return formattedJSON;
}

// format json in accordance to google Calendar chart requirements
function formattingCalendarChart(json) {
  var formattedJSON = '{';
  var firstElem = json[0];
  var keys = Object.keys(firstElem);
  var cols = '"cols":[';
  var type = '';
  for (var i = 0; i < keys.length; i++) {
    type = typeof firstElem[keys[i]];
    if (i == 0) {
      cols = cols + '{"label": "' + keys[i] + '", "type": "' + 'date' + '"' + '}';
    } else {
      cols = cols + '{"label": "' + keys[i] + '", "type": "number"' + '}';
    }
    if (i < keys.length - 1) {
      cols = cols + ',';
    }
  }
  cols = cols + '],';
  var rows = '"rows":[';
  count = 0;
  var keyCount = 0;
  for (var elem of json) {
    rows = rows + '{"c":[';
    keyCount = 0;
    for (var key of keys) {
      if (typeof elem[key] == 'number' || keyCount >= 1) {
        //document.getElementById("KeyCheck").innerHTML = typeof elem[key];
        rows = rows + '{"v":' + elem[key] + '}';
      } else {
        testing = rows = rows + '{"v":"Date(' + formatDate(elem[key]) + ')"}';
      }
      if (!(keyCount >= keys.length - 1)) {
        rows = rows + ',';
      }
      keyCount = keyCount + 1;
    }
    rows = rows + ']}';
    if (!(count >= json.length - 1)) {
      rows = rows + ',';
    }
    count = count + 1;
    //rows = rows + ",";
  }
  rows = rows + ']';
  formattedJSON = formattedJSON + cols + rows;
  formattedJSON = formattedJSON + '}';
  return formattedJSON;
}

// format json in accordance to google bar chart, column chart, and pie chart requirements
function formattingBarChart(json) {
  var formattedJSON = '{';
  var firstElem = json[0];
  var keys = Object.keys(firstElem);
  var cols = '"cols":[';
  var type = '';
  for (var i = 0; i < keys.length; i++) {
    type = typeof firstElem[keys[i]];
    if (i == 0) {
      cols = cols + '{"label": "' + keys[i] + '", "type": "' + type + '"' + '}';
    } else {
      cols = cols + '{"label": "' + keys[i] + '", "type": "number"' + '}';
    }
    if (i < keys.length - 1) {
      cols = cols + ',';
    }
  }
  cols = cols + '],';
  var rows = '"rows":[';
  count = 0;
  var keyCount = 0;
  for (var elem of json) {
    rows = rows + '{"c":[';
    keyCount = 0;
    for (var key of keys) {
      if (typeof elem[key] == 'number' || keyCount >= 1) {
        document.getElementById('KeyCheck').innerHTML = typeof elem[key];
        rows = rows + '{"v":' + elem[key] + '}';
      } else {
        rows = rows + '{"v":"' + elem[key] + '"}';
      }
      if (!(keyCount >= keys.length - 1)) {
        rows = rows + ',';
      }
      keyCount = keyCount + 1;
    }
    rows = rows + ']}';
    if (!(count >= json.length - 1)) {
      rows = rows + ',';
    }
    count = count + 1;
    //rows = rows + ",";
  }
  rows = rows + ']';
  formattedJSON = formattedJSON + cols + rows;
  formattedJSON = formattedJSON + '}';
  console.log('formatted: ' + formattedJSON);
  return formattedJSON;
}

// format json in accordance to google line chart requirements
function formattingLineChart(json) {
  var formattedJSON = '{';
  var firstElem = json[0];
  var keys = Object.keys(firstElem);
  var cols = '"cols":[';
  var type = '';
  for (var i = 0; i < keys.length; i++) {
    type = typeof firstElem[keys[i]];
    if (i == 0) {
      cols = cols + '{"label": "' + keys[i] + '", "type": "' + type + '"' + '}';
    } else {
      cols = cols + '{"label": "' + keys[i] + '", "type": "number"' + '}';
    }
    if (i < keys.length - 1) {
      cols = cols + ',';
    }
  }
  cols = cols + '],';
  var rows = '"rows":[';
  count = 0;
  var keyCount = 0;
  for (var elem of json) {
    rows = rows + '{"c":[';
    keyCount = 0;
    for (var key of keys) {
      if (typeof elem[key] == 'number' || keyCount >= 1) {
        document.getElementById('KeyCheck').innerHTML = typeof elem[key];
        rows = rows + '{"v":' + elem[key] + '}';
      } else {
        rows = rows + '{"v":"' + elem[key] + '"}';
      }
      if (!(keyCount >= keys.length - 1)) {
        rows = rows + ',';
      }
      keyCount = keyCount + 1;
    }
    rows = rows + ']}';
    if (!(count >= json.length - 1)) {
      rows = rows + ',';
    }
    count = count + 1;
    //rows = rows + ",";
  }
  rows = rows + ']';
  formattedJSON = formattedJSON + cols + rows;
  formattedJSON = formattedJSON + '}';
  return formattedJSON;
}

// format date to yyyy, mm, dd format, dictated by Google Charts API
function formatDate(date) {
  var d = new Date(date),
    month = '' + (d.getMonth() + 1),
    day = '' + d.getDate(),
    year = d.getFullYear();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join(', ');
}

// retrieve names of all databases present in API
function getDatabases() {
  var request = new XMLHttpRequest();
  request.onreadystatechange = function () {
    if (this.readyState == '4' && this.status == '200') {
      document.getElementById('Result').innerHTML = this.responseText;
      console.log('databasesResult: ' + this.responseText);
      var data = this.responseText;
      data = JSON.parse(data).databaseNames.split(', ');
      for (var i = 0; i < data.length; i++) {
        var selection = document.getElementById('DatabaseSelect');
        var opt = document.createElement('option');
        var name = '';
        if (i == 0) {
          name = data[i].substring(1, data[i].length);
        } else if (i == data.length - 1) {
          name = data[i].substring(0, data[i].length - 1);
        } else {
          name = data[i];
        }
        var present = false;
        for (var j = 0; j < document.getElementById('DatabaseSelect').options.length; j++) {
          if (document.getElementById('DatabaseSelect').options[j].value == name) {
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
      document.getElementById('Result').innerHTML =
        'Status: ' + this.status + ' readyState: ' + this.readyState;
    }
  };
  var path = apiOptions.APIURL + '/graphql/graphql?query=';
  path = path + 'query%7BdatabaseNames%7D';
  // replace curly parentheses in accordance with RFC 1738
  path = path.replace(/{/g, '%7B');
  path = path.replace(/}/g, '%7D');
  document.getElementById('Result').style.visibility = 'visible';
  document.getElementById('Result').innerHTML = path;
  request.open('GET', path, true);
  document.getElementById('Result').innerHTML = 'still running...';
  console.log('Path: ' + path);
  request.send();
}

// build report if new file is uploaded
function loadFile() {
  console.log('loadFile');
  var result = null;
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function () {
    console.log('Status: ' + this.status);
    if (this.readyState == '4' && this.status == '200') {
      result = xmlhttp.responseText;
      console.log('File read: ' + result);
      buildReport(result);
    }
  };
  xmlhttp.open('GET', 'savedQueries.json', true);
  xmlhttp.send();
}

// load queries from .json file
function loadQueries(event) {
  console.log('load Queries');
  var input = event.target;
  var reader = new FileReader();
  reader.onload = function () {
    buildReport(reader.result);
  };
  reader.readAsText(input.files[0]);
}

// build report from json object input
function buildReport(input) {
  var count = 0;
  console.log('Building Resport');
  while (document.getElementById('displayChart' + count) != null) {
    console.log('Building...');
    document
      .getElementById('displayChart' + count)
      .parentNode.removeChild(document.getElementById('displayChart' + count));
    count++;
  }
  var json = JSON.parse(input);
  var array = json.queries;
  for (var i = 0; i < array.length; i++) {
    console.log('Title: ' + array[i].options.title);
    var element = document.getElementById('reportView');
    var chartVisual = document.createElement('div');
    chartVisual.setAttribute('id', 'displayChart' + i);
    element.appendChild(chartVisual);
    executeRequest(array[i].query, array[i].visual, array[i].options, 'displayChart' + i);
  }
}

// set address of GraphQL URL in config.js
function setAPI(url) {
  apiOptions.APIURL = url;
}
