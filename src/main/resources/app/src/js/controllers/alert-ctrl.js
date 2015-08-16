/**
 * Alerts Controller
 */

angular
    .module('BookstoreApp')
    .controller('AlertsCtrl', ['$scope', AlertsCtrl]);

function AlertsCtrl($scope) {
    $scope.alerts = [{
        type: 'success',
        msg: 'Congratulations, you have yourself a nice-looking ebook server now'
    }, {
        type: 'danger',
        msg: "I'm still developing this application so some things might not be as expected"
    }];

    $scope.addAlert = function() {
        $scope.alerts.push({
            msg: 'Another alert!'
        });
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
}
