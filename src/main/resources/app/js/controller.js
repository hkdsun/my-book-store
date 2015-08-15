'use strict';

/* Controllers */

var bookstoreApp = angular.module('bookstoreApp', []);

bookstoreApp.controller('BookListCtrl', ['$scope', '$http', function($scope, $http) {
  $http.get('api/book').success(function(data) {
    $scope.books = data
  });

  $scope.orderProp = 'authors';
}]);
