function query() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (this.readyState == "4" && this.status == "200") {
			document.getElementById("Result").innerHTML = this.responseText;
		} else {
			document.getElementById("Result").innerHTML = "Status: " + this.status + " readyState: " + this.readyState;
		}
	}
	var requestType = document.getElementById("RequestType").value;
	var path = document.getElementById("APIHost").value;
	var input = document.getElementById("Request").value;
	// replace curly parentheses in accordance with RFC 1738
	input = input.replace(/{/g, "%7B");
	input = input.replace(/}/g, "%7D");
	path = path + "?input=" + input
	document.getElementById("Result").style.visibility = "visible";
	document.getElementById("Result").innerHTML = "running...";
	request.open("GET", path, true);
	document.getElementById("Result").innerHTML = "still running...";
	request.send();
	document.getElementById("Result").innerHTML = "further running...";
	document.getElementById("QueryCheck").innerHTML = path;
};

function toggleVisibility(id) {
	var elem = document.getElementById(id);
	if (elem.style.display === "none") {
		elem.style.display = "block";
	} else {
	elem.style.display = "none";
	}
};