function openNav() {
	document.getElementById("mySidenav").style.width = "20%";
	document.getElementById("main").style.marginLeft = "25%";
};

function closeNav() {
	document.getElementById("mySidenav").style.width = "0px";
	document.getElementById("main").style.marginLeft = "0%";
};

function toggleVisibility(id) {
	var elem = document.getElementById(id);
	if (elem.style.display === "none") {
		elem.style.display = "block";
	} else {
		elem.style.display = "none";
	}
};

function toggleSecond() {
	if (document.getElementById("DatabaseSelect").value == "All") {
		document.getElementById("SecondRequest").style.display = "block";
		document.getElementById("SecondRequest").required = true;
		document.getElementById("SecondRequestLabel").style.display = "block";
		document.getElementById("SecondRequestBreak").style.display = "block";
	} else {
		document.getElementById("SecondRequest").style.display = "none";
		document.getElementById("SecondRequest").required = false;
		document.getElementById("SecondRequestLabel").style.display = "none";
		document.getElementById("SecondRequestBreak").style.display = "none";
	}
};

function constructionInput() {
	document.getElementById("insertDiv").innerHTML = "";
	if (document.getElementById("TemplateType").value == "Media-User"){
		var formElement = document.getElementById("queryBuild");
		var insertDiv = document.getElementById("insertDiv")
		
		var artefactSelectLabel = document.createElement("label");
		artefactSelectLabel.innerHTML = "Select artefact:";
		var lineBreak = document.createElement("br");
		var artefactSelect = document.createElement("select");
		artefactSelect.setAttribute("id", "artefactSelect");
		var reviewOption = document.createElement("option");
		reviewOption.value = "Review";
		reviewOption.innerHTML = "Review";
		
		artefactSelect.appendChild(reviewOption);
		artefactSelectLabel.appendChild(lineBreak);
		artefactSelectLabel.appendChild(artefactSelect);
		insertDiv.appendChild(artefactSelectLabel);
		
		var userSelectLabel = document.createElement("label");
		userSelectLabel.innerHTML = "Select user by id:";
		lineBreak = document.createElement("br");
		var lineBreak2 = document.createElement("br");
		var userInput = document.createElement("input");
		userInput.setAttribute("type", "number");
		userInput.setAttribute("id", "userInput");
		
		userSelectLabel.appendChild(lineBreak);
		userSelectLabel.appendChild(userInput);
		insertDiv.appendChild(lineBreak2);
		insertDiv.appendChild(userSelectLabel);
	} else if (document.getElementById("TemplateType").value == "Media-Platform") {
		var formElement = document.getElementById("queryBuild");
		var insertDiv = document.getElementById("insertDiv")
		
		var artefactSelectLabel = document.createElement("label");
		artefactSelectLabel.innerHTML = "Select artefact:";
		var lineBreak = document.createElement("br");
		var artefactSelect = document.createElement("select");
		artefactSelect.setAttribute("id", "artefactSelect");
		var reviewOption = document.createElement("option");
		reviewOption.value = "Review";
		reviewOption.innerHTML = "Review";
		
		artefactSelect.appendChild(reviewOption);
		artefactSelectLabel.appendChild(lineBreak);
		artefactSelectLabel.appendChild(artefactSelect);
		insertDiv.appendChild(artefactSelectLabel);
		
		var platformSelectLabel = document.createElement("label");
		platformSelectLabel.innerHTML = "Select platform by link:";
		lineBreak = document.createElement("br");
		var lineBreak2 = document.createElement("br");
		var platformInput = document.createElement("input");
		platformInput.setAttribute("type", "text");
		platformInput.setAttribute("id", "platformInput");
		
		platformSelectLabel.appendChild(lineBreak);
		platformSelectLabel.appendChild(platformInput);
		insertDiv.appendChild(lineBreak2);
		insertDiv.appendChild(platformSelectLabel);
	}
};

function showNavbar() {
	document.getElementById("navigationBar").style.width = "50%";
	document.getElementById("pageContent").style.marginLeft = "50%";
	document.getElementById("navigationBar").style.display = "block";
};

function closeNavbar() {
	document.getElementById("navigationBar").style.width = "0%";
	document.getElementById("pageContent").style.marginLeft = "0.5%";
	document.getElementById("navigationBar").style.display = "none";
};

// handling oidc button
var signinCallback = function(result){
	console.log("Login");
	console.log("Result: " + result);
	if(result === "success"){
		// authenticated
		// OpenID Connect user info
		console.log(oidc_userinfo);
		document.getElementById("login").style.display = "none";
		document.getElementById("navigationBar").style.display = "block";
		document.getElementById("pageContent").style.display = "block";
	} else {
		// anonymous
		console.log("Login failed.");
	}
};