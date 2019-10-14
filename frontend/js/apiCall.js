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
	var path = "http://localhost:8080/GraphqlAPITest/graphql/graphql/testing/" + requestType;
	var input = document.getElementById("Request").value;
	//request.open("GET", path + input, true);
	document.getElementById("Result").style.visibility = "visible";
	document.getElementById("Result").innerHTML = "running...";
	request.open("GET", "http://localhost:8080/GraphqlAPITest/graphql/graphql/testing/query%7Bbw_author%7Bid,authorurl%7D%7D", true);
	document.getElementById("Result").innerHTML = "still running...";
	request.send();
	document.getElementById("Result").innerHTML = "further running...";
};

function toggleVisibility(id) {
	var elem = document.getElementById(id);
	if (elem.style.display === "none") {
		elem.style.display = "block";
	} else {
	elem.style.display = "none";
	}
};