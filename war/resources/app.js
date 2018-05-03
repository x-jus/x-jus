var app = angular.module('app', [ 'ngRoute', 'ngSanitize', 'angularUtils.directives.dirPagination' ]);

app.config(function($routeProvider, $locationProvider) {
	$routeProvider.when('/home', {
		templateUrl : '/resources/home.html',
		controller : 'ctrl'
	}).when('/index/:idx/search', {
		templateUrl : '/resources/search.html',
		controller : 'ctrlSearch'
	}).otherwise({
		redirectTo : '/home'
	});

	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(false);
});

app.controller('ctrlRouter', function($scope) {
	$scope.promise = null;
});

app.controller('ctrl', function($scope, $http, $location) {
	$scope.editing = undefined;

	$scope.indexes = [];

	$scope.status = function(i) {
		return $scope.indexes[i].active ? "Ativo" : "Inativo";
	}

	$scope.edit = function(i) {
		$scope.editing = i;
		$scope.idx = $scope.indexes[i].idx;
		$scope.descr = $scope.indexes[i].descr;
		$scope.api = $scope.indexes[i].api;
		$scope.token = $scope.indexes[i].token;
		$scope.active = $scope.indexes[i].active;
		$scope.maxBuild = $scope.indexes[i].maxBuild;
		$scope.maxRefresh = $scope.indexes[i].maxRefresh;
	}

	$scope.save = function() {
		var i = $scope.editing;
		if (i === -1) {
			$scope.indexes.push({});
			i = $scope.indexes.length - 1;
		}
		$scope.promise = $http({
			url : '/api/v1/index/' + $scope.idx,
			method : "POST",
			data : {
				idx : $scope.idx,
				descr : $scope.descr,
				api : $scope.api,
				token : $scope.token,
				active : $scope.active,
				maxBuild : $scope.maxBuild,
				maxRefresh : $scope.maxRefresh
			}
		}).then(function(response) {
			$scope.indexes[i] = response.data.index;
			$scope.editing = undefined;
		}, function(response) {
			$scope.apresentarProblema = true;
			$scope.msgProblema = response.data.errormsg;
		});
		// $scope.indexes[i].idx = $scope.idx;
		// $scope.indexes[i].descr = $scope.descr;
		// $scope.indexes[i].api = $scope.api;
		// $scope.indexes[i].token = $scope.token;
		// $scope.indexes[i].active = $scope.active;
	}

	$scope.remove = function() {
		$scope.promise = $http({
			url : '/api/v1/index/' + $scope.idx,
			method : "DELETE"
		}).then(function(response) {
			var i = $scope.editing;
			$scope.indexes.splice(i, 1);
			$scope.editing = undefined;
		}, function(response) {
			$scope.apresentarProblema = true;
			$scope.msgProblema = response.data.errormsg;
		});
	}

	$scope.create = function(i) {
		$scope.editing = -1;
		$scope.idx = undefined;
		$scope.descr = undefined;
		$scope.api = undefined;
		$scope.token = undefined;
		$scope.maxBuild = undefined;
		$scope.maxRefresh = undefined;
		$scope.active = false;
	}

	$scope.refresh = function(i) {
		$scope.promise = $http({
			url : '/api/v1/index/' + $scope.indexes[i].idx + "/refresh",
			method : "POST"
		}).then(function(response) {
		}, function(response) {
			$scope.apresentarProblema = true;
			$scope.msgProblema = response.data.errormsg;
		});
	}

	$scope.search = function(i) {
		$location.path('/index/' + $scope.indexes[i].idx + '/search');
	}

	$scope.promise = $http({
		url : '/api/v1/index',
		method : "GET"
	}).then(function(response) {
		$scope.indexes = response.data.list;
	}, function(response) {
		$scope.apresentarProblema = true;
		$scope.msgProblema = response.data.errormsg;
	});

});

app.controller('ctrlSearch', function($scope, $http, $templateCache, $interval, $window, $location, $routeParams) {
	$scope.promise = null;
	$scope.facetNames = [];
	$scope.facets = [];
	$scope.resultsPerPage = 5;
	$scope.params = $routeParams;

	$scope.pagination = {
		current : 1
	};

	$scope.addFacet = function(name, f) {
		var index = $scope.facets.indexOf(f);
		if (index == -1) {
			$scope.facetNames.push(name);
			$scope.facets.push(f);
			$scope.getResultsPage(1);
		}
	};

	$scope.removeFacet = function(name, f) {
		var index = $scope.facets.indexOf(f);
		if (index !== -1) {
			$scope.facets.splice(index, 1);
			$scope.facetNames.splice(index, 1);
		}
		$scope.getResultsPage(1);
	};

	$scope.pageChanged = function(newPage) {
		$scope.getResultsPage(newPage);
	};

	$scope.getResultsPage = function(pageNumber) {
		$scope.pagination.current = pageNumber;

		var f
		if ($scope.facets.length > 0) {
			for (var i = 0; i < $scope.facets.length; i++) {
				if (f)
					f += ",";
				else
					f = ""
				f += $scope.facets[i];
			}
		}

		$scope.promise = $http({
			url : '/api/v1/index/' + $scope.params.idx + '/query?filter=' + $scope.filter + '&page=' + $scope.pagination.current + '&perpage=' + $scope.resultsPerPage + (f ? '&facets=' + f : ''),
			method : "GET"
		}).then(function(response) {
			$scope.results = response.data;
		}, function(response) {
			$scope.apresentarProblema = true;
			$scope.msgProblema = response.data.error;
		});
	}

	$scope.$watch('filter', function() {
		$scope.getResultsPage(1);
	});

	$scope.show = function(key) {
		// $scope.promise = $http({
		// url : '/api/v1/locator?key=' + key,
		// method : "GET"
		// }).then(function(response) {
		// $location.path('/' + response.data.locator + '/show/' + key);
		// }, function(response) {
		// $scope.apresentarProblema = true;
		// $scope.msgProblema = response.data.error;
		// });
	};

	// angular.element('input#filter').focus();
	if ($scope.filter)
		$scope.getResultsPage(1);
});
