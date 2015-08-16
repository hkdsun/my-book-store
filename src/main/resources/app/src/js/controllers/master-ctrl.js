/**
 * Master Controller
 */

angular.module('BookstoreApp')
    .controller('MasterCtrl', ['$scope', '$http', '$cookieStore', MasterCtrl]);

function MasterCtrl($scope, $http, $cookieStore) {
    /**
     * Sidebar Toggle & Cookie Control
     */
    var mobileView = 992;

    $scope.getWidth = function() {
        return window.innerWidth;
    };

    $scope.$watch($scope.getWidth, function(newValue, oldValue) {
        if (newValue >= mobileView) {
            if (angular.isDefined($cookieStore.get('toggle'))) {
                $scope.toggle = ! $cookieStore.get('toggle') ? false : true;
            } else {
                $scope.toggle = true;
            }
        } else {
            $scope.toggle = false;
        }

    });

    $scope.toggleSidebar = function() {
        $scope.toggle = !$scope.toggle;
        $cookieStore.put('toggle', $scope.toggle);
    };

    window.onresize = function() {
        $scope.$apply();
    };

    $http.get('api/book').success(function(data) {
      $scope.books = data;
    });

    $scope.setBookId = function (selectedId) {
      $http.get('api/book/' + selectedId).success(function(data) {
        $scope.selectedBook = data;
      });
    }

    $scope.query = {
      field: ''
    };

}
