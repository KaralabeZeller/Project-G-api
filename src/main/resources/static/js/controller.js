(function () {
    'use strict';

    var messageForm = document.querySelector('#messageForm'),
        messageArea = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        lobbyHeader = document.getElementById('lobbyHeader');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    // TODO implement - url parameters for controller - username
    var userName = sessionStorage.getItem('name');
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
            sender: userName,
            type: 'JOIN',
        };
        stompClient.send('/app/chat.addUser', {}, JSON.stringify(message));

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
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));

        started = true;

        event.preventDefault();
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        var type = message.type;
        if (type === 'JOIN' || type === 'LEAVE') {
            if (!started) {
                var split = message.content.split(',');
                users.length = 0;
                users.push(...split);
                displayUsers();
            }
        } else if (type === 'START') {
            playSecretHitler();
        } else if (type === 'GAME') {
            var gameType = message.gameType;
            if (gameType === 'FACTION') {
                displayFaction(message.content);
            } else if (gameType === 'QUERY_CHANCELLOR') {
                nominateChancellor(message.content.split(','));
            } else if (gameType === 'VOTE') {
                vote(message.content.split(','));
            } else {
              // TODO other messages
            }
        }
    }

    function sendReply(type, content) {
        var message = {
            type: 'GAME',
            gameType: type,
            sender: userName,
            content: content,
        };
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
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

    function nominateChancellor(players) {
         showDialog("QUERY_CHANCELLOR", "Nominate the chancellor for the government", players)
    }

    function vote(players) {
         showDialog('VOTE', 'Vote for the government:', players);
    }

    function showDialog(type, titleText, options) {
         var optionList = [];
         for (var i = 0; i < options.length; i++) { optionList.push({ text: options[i], value: options[i] }); }

         //TODO remove - cancel button from prompt
         bootbox.prompt({
            closeButton: false,
            title: titleText,
            value: options[0],
            inputType: 'select',
            inputOptions: optionList,
            callback: function (result) {
               if(result === null)
                   showDialog(type, titleText, options);
                else
                    sendReply(type, result);
            }
         });
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

}());
