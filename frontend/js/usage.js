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