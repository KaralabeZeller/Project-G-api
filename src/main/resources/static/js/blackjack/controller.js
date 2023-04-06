(function () {
    'use strict';

    var messageForm       = document.querySelector('#messageForm'),
        messageArea       = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        firstCard         = document.createElement('img'),
        secondCard        = document.createElement('img'),
        lobbyHeader       = document.getElementById('lobbyHeader');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient;
    var subscriptionLobby;
    var subscriptionPublic;
    var subscriptionUser;

    // TODO use url parameters for controller (lobby id, username)
    var userName = document.getElementById('userName').value;
    var lobbyName = document.getElementById('lobbyName').value;
    var users = [];

    var started = false;

    connect();

    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        subscriptionLobby = stompClient.subscribe('/topic/lobby/' + lobbyName, onMessageReceived);
        subscriptionPublic = stompClient.subscribe('/topic/game/' + lobbyName, onMessageReceived);
        subscriptionUser = stompClient.subscribe('/user/topic/game/' + lobbyName, onMessageReceived);

        sendLobby('JOIN', null);

        connectingElement.classList.add('hidden');
    }

    function onError(error) {
        console.log('onError: Could not connect to WebSocket server. Please refresh this page to try again!', error);

        connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
        connectingElement.style.color = 'red';
    }

    function disconnect() {
        subscriptionLobby.unsubscribe();
        subscriptionPublic.unsubscribe();
        subscriptionUser.unsubscribe();

        stompClient.disconnect(onDisconnected);
    }

    function onDisconnected() {
        console.log('DISCONNECTED');
    }

    function startGame(event) {
        sendStart('BlackJack');

        started = true;

        event.preventDefault();
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        var type = message.type;
        if (type === 'LOBBY') {
            var lobbyType = message.lobbyType;
            if (lobbyType === 'JOIN' || lobbyType === 'LEAVE' && !started) {
                var split = message.content.split(',');
                users.length = 0;
                users.push(...split);
                displayUsers();
            }
        } else if (type === 'START') {
//            subscriptionLobby.unsubscribe();
            subscriptionPublic.unsubscribe();
            playBlackJack();
        } else if (type === 'GAME') {
            var gameType = message.gameType;
            if (gameType === 'DEAL') {
                updateCard(message);
            } else {
                console.log('Ignoring game message: %s', message);
            }
        } else if (type === 'STOP') {
            disconnect();
        } else {
            console.log('Ignoring other message: %s', message);
        }
    }


    function updateCard(message) {
        var cards = message.content.split(',');
        var cardName1 =  '/games/common/cards/poker/' + cards[0] + '.svg';
        console.log('Showing card: %s', cardName1);
        firstCard.src = cardName1;

        var cardName2 =  '/games/common/cards/poker/' + cards[1] + '.svg';
        console.log('Showing card: %s', cardName2);
        secondCard.src = cardName2;
    }

    function playBlackJack() {
            startButton.classList.add('hidden');
            lobbyHeader.innerHTML = userName;
            messageArea.innerHTML = '';
            displayCard();
    }

       function displayCard() {
            var firstCardElement  = document.createElement('li'),
                secondCardElement = document.createElement('li');

            firstCardElement.classList.add('event-message');
            secondCardElement.classList.add('event-message');

            firstCard.width = 170;
            firstCard.height = 210;
            firstCard.id = 'factionCard';
            firstCard.src = '/games/secrethitler/role-cover.png';

            secondCard.width = 170;
            secondCard.height = 210;
            secondCard.id = 'membershipCard';
            secondCard.src = '/games/secrethitler/role-cover.png';

            firstCardElement.appendChild(firstCard);
            firstCardElement.appendChild(secondCard);

            messageArea.appendChild(firstCardElement);
            messageArea.scrollTop = messageArea.scrollHeight;
        }

    function sendLobby(type, content) {
        var message = {
            type: 'LOBBY',
            lobbyType: type,
            sender: userName,
            lobby: lobbyName,
            content: content,
        };
        stompClient.send('/app/lobby/' + lobbyName, {}, JSON.stringify(message));
    }

    function sendStart(content) {
        var message = {
            type: 'START',
            sender: userName,
            lobby:  lobbyName,
            content: content,
        };
        stompClient.send('/app/game/' + lobbyName, {}, JSON.stringify(message));
    }

    function sendGame(type, content) {
        var message = {
            type: 'GAME',
            gameType: type,
            sender: userName,
            lobby:  lobbyName,
            content: content,
        };
        stompClient.send('/app/game/' + lobbyName, {}, JSON.stringify(message));
    }

    // TODO move to a different file with all the other lobby stuff
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

            messageArea.appendChild(messageElement);
            messageArea.scrollTop = messageArea.scrollHeight;
        });

        if (users.length >= 1 && users.length <= 10) {
            startButton.classList.remove('hidden');
        } else {
            startButton.classList.add('hidden');
        }
    }

    function showDialog(type, title, options, multiChoice = false) {
        // TODO remove cancel button from prompt or implement cancel vote on cancel button
        bootbox.prompt({
            // buttons: { confirm: { label: 'OK' } },
            closeButton: false,
            title: title,
            inputOptions: options.map(option => ({ text: option, value: option })),
            inputType: multiChoice ? 'checkbox' : 'select',
            value:  multiChoice ?  null : options[0],
            callback: result => {
                if (result) {
                    sendGame(type, multiChoice ? result.join(',') : result);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    function getAvatarColor(name) {
        var hash = 0;
        for (var i = 0; i < name.length; i++) {
            hash = 31 * hash + name.charCodeAt(i);
        }
        var index = Math.abs(hash % colors.length);
        return colors[index];
    }

    messageForm.addEventListener('submit', startGame, true);

}());
