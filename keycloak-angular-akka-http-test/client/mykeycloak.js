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
        }).error(function() {
	    console.log("Error " + keycloak.token);
            //alert('failed to initialize');
        });
	    
}

initializeKeycloak();
