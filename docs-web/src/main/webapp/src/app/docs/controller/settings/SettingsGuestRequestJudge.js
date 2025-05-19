'use strict';

/**
 * Settings guest request approval page controller.
 */
angular.module('docs').controller('SettingsGuestRequestJudge', function($scope, $dialog, $state, $stateParams, Restangular, $translate) {
  /**
   * Load the current guest request.
   */
  Restangular.one('guest_request', $stateParams.id).get().then(function (data) {
    $scope.request = data;
  });

  /**
   * Judge the current request.
   */
  $scope.judge = function (approved_val) {
    if (typeof approved_val !== 'boolean') {
      console.error('approved_val must be a boolean');
      return;
    }

    var request = angular.copy($scope.request);
    Restangular
      .one('guest_request', $stateParams.id)
      .post('', { approved: approved_val })
      .then(function () {
        var title = $translate.instant('settings.guestrequest.judge.request_success_title');
        var msg = $translate.instant('settings.guestrequest.judge.request_success_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
          $dialog.messageBox(title, msg, btns).open().then(function () {
            location.reload(true);
          });
        }, function (e) {
        if (e.data.type === 'RequestNotFound') {
          var title = $translate.instant('settings.guestrequest.judge.request_not_found_title');
          var msg = $translate.instant('settings.guestrequest.judge.request_not_found_message');
          var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
          $dialog.messageBox(title, msg, btns);
        }
      });
  };
});