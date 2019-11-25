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