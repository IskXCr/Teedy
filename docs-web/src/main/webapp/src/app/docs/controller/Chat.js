'use strict';

angular.module('docs').controller('Chat', function($scope, $dialog, Restangular, $translate, $interval) {
  $scope.chatMessages = [];
  $scope.newChatMessage = '';

  function loadMessages() {
    Restangular.one('chat_message').get().then(function(data) {
      $scope.chatMessages = data.chat_messages;
    }, function (response) {
      $scope.chatMessagesError = response;
    });
  }

  $scope.sendMessage = function() {
    if (!$scope.newChatMessage || $scope.newChatMessage.length === 0) {
        return;
    }
    console.log($scope.newChatMessage)
    Restangular.one('chat_message').put({ content: $scope.newChatMessage }).then(function() {
      $scope.newChatMessage = '';
      loadMessages();
    });
  };

  $scope.deleteMessage = function(message) {
      var title = $translate.instant('chat.delete_message_title');
      var msg = $translate.instant('chat.delete_message_message');
      var btns = [
          {result: 'cancel', label: $translate.instant('cancel')},
          {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
      ];

      $dialog.messageBox(title, msg, btns, function(result) {
          if (result === 'ok') {
              Restangular.one('chat_message', message.id).remove().then(loadMessages);
          }
      });
  };

  loadMessages();

  var intervalPromise = $interval(loadMessages, 1000);
  // Clean up interval when controller is destroyed
  $scope.$on('$destroy', function() {
    $interval.cancel(intervalPromise);
  });
});