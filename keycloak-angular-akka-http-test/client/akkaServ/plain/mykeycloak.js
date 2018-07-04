function initializeKeycloak() {
	console.log("start function initalizieKeycloak");
		var keycloakConfig = {
  "realm": "apiserver",
  "url": "http://172.18.0.1:8080/auth/",
  "clientId" : "webclient"
  };
	  var keycloak = Keycloak(keycloakConfig);
	  console.log("set config");
	keycloak.init({
	    "onLoad": 'login-required'
	  }).success(function(authenticated) {
		console.log("Authen: " + authenticated);
            alert(authenticated ? 'authenticated' : 'not authenticated');
	    document.getElementById("userid").innerHTML = keycloak.tokenParsed.name;
	    document.getElementById("token").innerHTML = keycloak.token;
	performCallback(keycloak.token);
        }).error(function() {
	    console.log("Error ");
            //alert('failed to initialize');
        });
	    
}

function performCallback(token){

    var url = 'http://localhost:9000/test?callback=JSON_CALLBACK';
    var url2 = 'http://localhost:9000/webadmin';
    var req = new XMLHttpRequest();
    req.open('GET', url, true);
    req.withCredentials = true;
    req.setRequestHeader('Accept', 'text/plain');
    req.setRequestHeader('Authorization',  token);
    req.setRequestHeader('Cookies', "token="+token)

    req.onreadystatechange = function () {
        if (req.readyState == 4) {
            if (req.status == 200) {
                alert('Success' + req.response);
            } else if (req.status == 403) {
                alert('Forbidden' + req.response);
            }
        }
    }

    req.send();
}

initializeKeycloak();
