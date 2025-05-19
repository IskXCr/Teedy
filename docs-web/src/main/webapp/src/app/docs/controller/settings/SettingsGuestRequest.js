'use strict';

/**
 * Settings guest request page controller.
 */
angular.module('docs').controller('SettingsGuestRequest', function($scope, $state, Restangular) {
    /**
     * Load guest requests from server.
     */
    $scope.loadGuestRequests = function() {
      Restangular.one('guest_request/list').get({
        sort_column: 1,
        asc: true
      }).then(function(data) {
        $scope.guest_requests = data.guest_requests;
      });
    };
    
    $scope.loadGuestRequests();
    
    /**
     * Judge a guest request using ID
     */
    $scope.judgeRequest = function(guestRequest) {
      $state.go('settings.guestrequest.judge', { id: guestRequest.id });
    };
  });