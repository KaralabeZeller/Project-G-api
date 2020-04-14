(function () {
    'use strict';

    var chatPage = document.querySelector('#chat-page'),
        messageForm = document.querySelector('#messageForm'),
        messageInput = document.querySelector('#message'),
        messageArea = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        lobbyText = document.getElementById("LobbyText");

    var userDialog = document.getElementById('userDialog'),
        selectEl = document.getElementById('selector'),
        confirmBtn = document.getElementById('confirmDialog');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
            '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    var username = null;
    var userList = [];

    var isStarted = false;

    var liberalPolicies = 0;
    var fascistPolicies = 0;

    //TODO implement - url parameters for screen - lobby id and controller - username
    var stringData = sessionStorage.getItem("name");
    var obj = JSON.parse(stringData);
    if(obj !== null) {
        username = obj;
        chatPage.classList.remove('hidden');
        chatPage.classList.remove('hidden');
    }

    connect();

    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.reconnect_delay = 500;
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        // Subscribe to the Public Topic
        stompClient.subscribe('/topic/public', onMessageReceived);
        stompClient.subscribe('/user/topic/public', onMessageReceived);

        // Tell your username to the server
        var message = {
            sender : username,
            type : 'JOIN',
        };
        stompClient.send("/app/chat.addUser", {}, JSON.stringify(message));

        connectingElement.classList.add('hidden');

    }

    function onError(error) {
        connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
        connectingElement.style.color = 'red';
    }


    function sendMessage(event) {
        var messageContent = messageInput.value.trim();
        if (messageContent) {
            var message = {
                type : 'CHAT',
                sender : username,
                content : messageInput.value,
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));

            messageInput.value = '';
        }

        event.preventDefault();
    }

    function startGame(event) {
        var message = {
            type : 'START',
            sender : username,
            content : 'SecretHitler',
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));

        isStarted = true;

        event.preventDefault();
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        if (message.type === 'JOIN' || message.type === 'LEAVE') {
            if (!isStarted) {
                var splitted = message.content.split(','),
                    i = 0;

                var element = document.getElementById('messageArea');
                element.innerHTML = '';

                userList.length = 0;
                for (i = 0; i < splitted.length; i++) {
                    userList.push(splitted[i]);

                    var messageElement = document.createElement('li');
                    messageElement.classList.add('chat-message');

                    var avatarElement = document.createElement('i');
                    var avatarText = document.createTextNode(splitted[i][0]);
                    avatarElement.appendChild(avatarText);
                    avatarElement.style['background-color'] = getAvatarColor(splitted[i]);
                    messageElement.appendChild(avatarElement);

                    var usernameElement = document.createElement('span');
                    var usernameText = document.createTextNode(splitted[i]);
                    usernameElement.appendChild(usernameText);
                    messageElement.appendChild(usernameElement);

                    var textElement = document.createElement('p');
                    var messageText = document.createTextNode('');
                    textElement.appendChild(messageText);
                    messageElement.appendChild(textElement);

                    messageArea.appendChild(messageElement);
                    messageArea.scrollTop = messageArea.scrollHeight;
                }
            }

            if (userList.length >= 2) {
                startButton.classList.remove('hidden');
            } else {
                startButton.classList.add('hidden');
            }
        } else if (message.type === 'START') {
            playSecretHitler();
        } else if (message.type === 'GAME') {
            if (message.gameMessageType === 'FACTION') {
                displayFaction(message.content);
            } else if (message.gameMessageType === 'QUERY_CHANCELLOR') {
                nominateChancellor(message.content);
            } else {
              // TODO other messages
            }
        }
    }

    function nominateChancellor(players) {
         var splitted = players.split(','),
             i = 0,
             output = '',
             techSelectOptions = document.querySelector("select");

         document.getElementById('labelDialog').innerHTML = 'Choose a chancellor: ';

         for (i = 0; i < splitted.length; i++) {
              output += '<option>'+ splitted[i] + '</option>';
         }

         selectEl.innerHTML = output;

         userDialog.showModal();
    }

    selectEl.addEventListener('change', function onSelect(e) {
      confirmBtn.value = selectEl.value;
    });

    userDialog.addEventListener('close', function onClose() {
      var value = 'Chancellor selected: ' +userDialog.returnValue;
      console.log(value);
      sendReply('QUERY_CHANCELLOR', userDialog.returnValue);
    });

    function sendReply(type, value) {
        var messageContent = value;

        if (messageContent) {
            var message = {
                type : 'GAME',
                sender : username,
                content : value,
                gameMessageType : type,
            };

            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));

        }
    }

    function getAvatarColor(messageSender) {
        var hash = 0;
        for (var i = 0; i < messageSender.length; i++) {
            hash = 31 * hash + messageSender.charCodeAt(i);
        }
        var index = Math.abs(hash % colors.length);
        return colors[index];
    }

    function playSecretHitler() {
        startButton.classList.add('hidden');

        initController();
    }

    function initController() {
        lobbyText.innerHTML = username;
        var element = document.getElementById('messageArea');
        element.innerHTML = '';
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

    messageForm.addEventListener('submit', startGame, true)

    //TODO implement - add css for the Modal panel
}());
