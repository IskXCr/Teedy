'use strict';

angular.module('docs').controller('GuestRequest', function($scope, $dialog, Restangular, $translate, $interval) {
  $scope.user = {}; // Very important otherwise ng-if in template will make a new scope variable

  /**
   * Submit the request.
   */
  $scope.edit = function () {
    var promise = null;
    var user = angular.copy($scope.user);
    user.storage_quota *= 1000000;
    
    promise = Restangular
      .one('guest_request')
      .put(user);

    promise.then(function () {
      var title = $translate.instant('guestrequest.request_success_title');
      var msg = $translate.instant('guestrequest.request_success_message');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns).open().then(function () {
      location.reload(true);
      });
    }, function (e) {
      var title = $translate.instant('guestrequest.error.title');
      var msg = e.data && e.data.message ? e.data.message : $translate.instant('guestrequest.error.unknown');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns).open();
    });
  };
});