'use strict';

(function () {

	angular.module('KeycloakApp', ['ngCookies']);

	function initializeKeycloak() {
		var keycloakConfig = {
		  "url": "http://localhost:8080/auth",
		  "realm": "apiserver",
		  "clientId": "webclient",
 	"credentials": {
    "secret": "d9a738d1-43d6-45f2-9d7a-28abca9e541e"
  }
		};
	  var keycloak = Keycloak(keycloakConfig);
	  keycloak.init({
	    onLoad: 'login-required'
	  }).success(function () {
	    keycloak.loadUserInfo().success(function (userInfo) {
	      bootstrapAngular(keycloak, userInfo);
	    });
	  });
	}

  function bootstrapAngular(keycloak, userInfo) {
    angular.module('KeycloakApp')
      .run(function ($rootScope, $http, $interval, $cookies) {
				var updateTokenInterval = $interval(function () {
          // refresh token if it's valid for less then 15 minutes
          keycloak.updateToken(15)
            .success(function (refreshed) {
              if (refreshed) {
                $cookies.put('X-Authorization-Token', keycloak.token);
              }
            });
        }, 10000);

        $cookies.put('X-Authorization-Token', keycloak.token);

        $rootScope.userLogout = function () {
          $cookies.remove('X-Authorization-Token');
					$interval.cancel(updateTokenInterval);
          keycloak.logout();
        };

				$rootScope.authData = {};

				$http.jsonp("http://localhost:9000/test?callback=JSON_CALLBACK")
					.success(function (response) {
						$rootScope.authData.token = response.token;
						$rootScope.authData.username = response.username;
					});
      });

    angular.bootstrap(document, ['KeycloakApp']);
  }

	initializeKeycloak();

}());
