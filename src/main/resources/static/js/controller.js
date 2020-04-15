(function () {
    'use strict';

    var messageForm = document.querySelector('#messageForm'),
//        messageInput = document.querySelector('#message'),
        messageArea = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        lobbyText = document.getElementById("LobbyText");

    var userDialog = document.getElementById('userDialog'),
        userDialogLabel = document.getElementById('userDialogLabel'),
        userDialogSelect = document.getElementById('userDialogSelector'),
        confirmButton = document.getElementById('confirmButton');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    // TODO implement - url parameters for screen - lobby id and controller - username
    var username = sessionStorage.getItem('name');
    var users = [];

    var started = false;

    var liberalPolicies = 0;
    var fascistPolicies = 0;

    connect();

    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        stompClient.subscribe('/topic/public', onMessageReceived);
        stompClient.subscribe('/user/topic/public', onMessageReceived);

        var message = {
            sender: username,
            type: 'JOIN',
        };
        stompClient.send("/app/chat.addUser", {}, JSON.stringify(message));

        connectingElement.classList.add('hidden');
    }

    function onError(error) {
        console.log('onError: Could not connect to WebSocket server. Please refresh this page to try again!', error);

        connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
        connectingElement.style.color = 'red';
    }

//    function sendMessage(event) {
//        var messageContent = messageInput.value.trim();
//        if (messageContent) {
//            var message = {
//                type: 'CHAT',
//                sender: username,
//                content: messageInput.value,
//            };
//            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
//
//            messageInput.value = '';
//        }
//
//        event.preventDefault();
//    }

    function startGame(event) {
        var message = {
            type: 'START',
            sender: username,
            content: 'SecretHitler',
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));

        started = true;

        event.preventDefault();
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        if (message.type === 'JOIN' || message.type === 'LEAVE') {
            if (!started) {
                var split = message.content.split(','),
                    i = 0;

                var element = document.getElementById('messageArea');
                element.innerHTML = '';

                users.length = 0;
                users.push(...split);

                users.forEach(user => {
                    var messageElement = document.createElement('li');
                    messageElement.classList.add('chat-message');

                    var avatarElement = document.createElement('i');
                    var avatarText = document.createTextNode(user[0]);
                    avatarElement.appendChild(avatarText);
                    avatarElement.style['background-color'] = getAvatarColor(user);
                    messageElement.appendChild(avatarElement);

                    var usernameElement = document.createElement('span');
                    var usernameText = document.createTextNode(user);
                    usernameElement.appendChild(usernameText);
                    messageElement.appendChild(usernameElement);

                    var textElement = document.createElement('p');
                    var messageText = document.createTextNode('');
                    textElement.appendChild(messageText);
                    messageElement.appendChild(textElement);

                    messageArea.appendChild(messageElement);
                    messageArea.scrollTop = messageArea.scrollHeight;
                });
            }

            if (users.length >= 2) {
                startButton.classList.remove('hidden');
            } else {
                startButton.classList.add('hidden');
            }
        } else if (message.type === 'START') {
            initController();
            playSecretHitler();
        } else if (message.type === 'GAME') {
            if (message.gameMessageType === 'FACTION') {
                displayFaction(message.content);
            } else if (message.gameMessageType === 'QUERY_CHANCELLOR') {
                nominateChancellor(message.content.split(','));
            } else {
              // TODO other messages
            }
        }
    }

    function sendReply(type, content) {
        var message = {
            type: 'GAME',
            sender: username,
            content: content,
            gameMessageType: type,
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
    }

    function initController() {
        startButton.classList.add('hidden');
        lobbyText.innerHTML = username;
        messageArea.innerHTML = '';
    }

    function playSecretHitler() {
        // TODO implement
    }

    function displayFaction(faction) {
         var messageElement = document.createElement('li');
         messageElement.classList.add('event-message');

         var textElement = document.createElement('p');
         var messageText = document.createTextNode('You are: ' + faction);
         textElement.appendChild(messageText);
         messageElement.appendChild(textElement);

         messageArea.appendChild(messageElement);
         messageArea.scrollTop = messageArea.scrollHeight;
    }

    function nominateChancellor(players) {
         var options = players.map(player => '<option>' + player + '</option>').join('');

         userDialogLabel.innerHTML = 'Choose a chancellor: ';
         userDialogSelect.innerHTML = options;
         userDialog.showModal();
    }

    function onUserDialogSelect(e) {
        confirmButton.value = userDialogSelect.value;
    }

    function onUserDialogClose() {
        var value = userDialog.returnValue

        if(value === 'default')
            value = userDialogSelect.getElementsByTagName('option')[0];

        console.log('Chancellor selected: ' + value);
        sendReply('QUERY_CHANCELLOR', value);
    }

    function getAvatarColor(messageSender) {
        var hash = 0;
        for (var i = 0; i < messageSender.length; i++) {
            hash = 31 * hash + messageSender.charCodeAt(i);
        }
        var index = Math.abs(hash % colors.length);
        return colors[index];
    }

    messageForm.addEventListener('submit', startGame, true);
    userDialogSelect.addEventListener('change', onUserDialogSelect, true);
    userDialog.addEventListener('close', onUserDialogClose, true)

    //TODO implement - add css for the Modal panel
}());
