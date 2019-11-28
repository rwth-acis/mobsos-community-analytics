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

// handling for oidc button
function signinCallback(result) {
	if(result === "success"){
	    // after successful sign in, display a welcome string for the user
		//$("#status").html("Hello, " + oidc_userinfo.name + "!");
	} else {
	    // if sign in was not successful, log the cause of the error on the console
		console.log(result);
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