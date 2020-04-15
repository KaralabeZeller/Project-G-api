(function () {
    'use strict';

    var messageForm = document.querySelector('#messageForm'),
        messageArea = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        lobbyHeader = document.getElementById("lobbyHeader");

    var userDialog = document.getElementById('userDialog'),
        userDialogLabel = document.getElementById('userDialogLabel'),
        userDialogSelect = document.getElementById('userDialogSelector'),
        confirmButton = document.getElementById('confirmButton');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    // TODO implement - url parameters for screen - lobby id and controller - username
    var userName = sessionStorage.getItem('name');
    var users = [];

    var started = false;
    var gameStatus = null;

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
            sender: userName,
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

    function startGame(event) {
        var message = {
            type: 'START',
            sender: userName,
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
                var split = message.content.split(',');
                users.length = 0;
                users.push(...split);

            }
            // TODO revert later - temporarily always display users on update
            displayUsers();
        } else if (message.type === 'START') {
            playSecretHitler();
        } else if (message.type === 'GAME') {
            if (message.gameMessageType === 'FACTION') {
                displayFaction(message.content);
            } else if (message.gameMessageType === 'QUERY_CHANCELLOR') {
                var split = message.content.split(',');
                nominateChancellor(split);
            } else {
              // TODO other messages
            }
        }
    }

    function sendReply(type, content) {
        var message = {
            type: 'GAME',
            gameMessageType: type,
            sender: userName,
            content: content,
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
    }

    function displayUsers() {
        messageArea.innerHTML = '';
        users.forEach(user => {
            var messageElement = document.createElement('li');
            messageElement.classList.add('chat-message');

            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(user[0]);
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(user);
            messageElement.appendChild(avatarElement);

            var userNameElement = document.createElement('span');
            var userNameText = document.createTextNode(user);
            userNameElement.appendChild(userNameText);
            messageElement.appendChild(userNameElement);

            var textElement = document.createElement('p');
            var messageText = document.createTextNode('');
            textElement.appendChild(messageText);
            messageElement.appendChild(textElement);

            messageArea.appendChild(messageElement);
            messageArea.scrollTop = messageArea.scrollHeight;
        });

            if (users.length >= 5 && users.length <= 10) {
                startButton.classList.remove('hidden');
            } else {
                startButton.classList.add('hidden');
            }
        } else if (message.type === 'START') {
            initController();
            playSecretHitler();
        } else if (message.type === 'GAME') {
            gameStatus = message.gameMessageType;

            if (message.gameMessageType === 'FACTION') {
                displayFaction(message.content);
            } else if (message.gameMessageType === 'QUERY_CHANCELLOR') {
                nominateChancellor(message.content.split(','));
            } else if (message.gameMessageType === 'VOTE') {
                vote(message.content.split(','));
            }else {
              // TODO other messages
            }
        }
    }

    function playSecretHitler() {
        startButton.classList.add('hidden');
        lobbyHeader.innerHTML = userName;
        messageArea.innerHTML = '';
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

    //TODO implement - nominateChancellor and vote can be the same function
    function nominateChancellor(players) {
         var options = players.map(player => '<option>' + player + '</option>').join('');

         // TODO implement - add css for the Modal panel
         userDialogLabel.innerHTML = 'Choose a chancellor: ';
         userDialogSelect.innerHTML = options;
         userDialog.showModal();
    }

    function vote(players) {
         var options = players.map(player => '<option>' + player + '</option>').join('');

         userDialogLabel.innerHTML = 'Vote for the government:';
         userDialogSelect.innerHTML = options;
         userDialog.showModal();
    }

    function onUserDialogSelect(e) {
        confirmButton.value = userDialogSelect.value;
    }

    function onUserDialogClose() {
        var value = userDialog.returnValue
        if (value === 'default') {
            value = userDialogSelect.getElementsByTagName('option')[0].innerHTML;
        }

        console.log('Selected: ' + value);
        sendReply(gameStatus, value);
        userDialogSelect.innerHTML = '';
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

}());
