(function () {
    'use strict';

    var messageForm       = document.querySelector('#messageForm'),
        messageArea       = document.querySelector('#messageArea'),
        connectingElement = document.querySelector('.connecting'),
        lobbyHeader       = document.getElementById('lobbyHeader'),
        factionCard       = document.createElement('img'),
        membershipCard    = document.createElement('img');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;
    var subscriptionPublic;
    var subscriptionUser;

    // TODO use url parameters for controller (lobby id, username)
    var userName = sessionStorage.getItem('name');
    var users = [];

    var started = false;
    var factionShow = false;
    var membershipShow = false;

    var liberalPolicies = 0;
    var fascistPolicies = 0;

    connect();

    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        subscriptionPublic = stompClient.subscribe('/topic/public', onMessageReceived);
        subscriptionUser = stompClient.subscribe('/user/topic/public', onMessageReceived);

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

    function disconnect() {
        subscriptionPublic.unsubscribe();
        subscriptionUser.unsubscribe();

        stompClient.disconnect(onDisconnected);
    }

    function onDisconnected() {
        console.log('DISCONNECTED');
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
            } else if (gameType === 'POLICIES') {
                showDialog('POLICIES', 'Choose two policies to hand over to the chancellor', message.content.split(','), true);
            } else if (gameType === 'POLICY') {
                showDialog('POLICY', 'Choose a policy, which will be enacted', message.content.split(','));
            } else if (gameType === 'TOP_POLICIES') {
                showTopPolicies(message.content);
            } else if (gameType === 'KILL') {
                showDialog('KILL', 'Choose a player to be killed', message.content.split(','));
            }  else if (gameType === 'INVESTIGATE') {
                showDialog('INVESTIGATE', 'Choose a player to be investigated', message.content.split(','));
            } else if (gameType === 'INVESTIGATE_RESULT') {
                showInvestigatedFaction(message.content);
            } else if (gameType === 'SPECIAL_ELECTION') {
                showDialog('SPECIAL_ELECTION', 'Choose a player to be elected as president', message.content.split(','));
            } else if (gameType === 'KILLED') {
                disconnect();
            } else {
                console.log('Ignoring game message: %s', message);
            }
        } else if (type === 'STOP') {
            disconnect();
        } else {
            console.log('Ignoring other message: %s', message);
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

        if (users.length >= 5 && users.length <= 10) {
            startButton.classList.remove('hidden');
        } else {
            startButton.classList.add('hidden');
        }

    }

    function playSecretHitler() {
        subscriptionPublic.unsubscribe();
        startButton.classList.add('hidden');
        lobbyHeader.innerHTML = userName;
        messageArea.innerHTML = '';
    }

    function displayFaction(faction) {
        var factionElement = document.createElement('li'),
            membershipElement = document.createElement('li');

        factionElement.classList.add('event-message');
        membershipElement.classList.add('event-message');

        factionCard.width = 210;
        factionCard.height = 295;
        factionCard.id = 'factionCard';
        factionCard.onclick = event => showFaction(faction);
        showFaction(faction);

        membershipCard.width = 172;
        membershipCard.height = 233;
        membershipCard.id = 'membershipCard';
        membershipCard.onclick = event => showMembership(faction);
        showMembership(faction);

        factionElement.appendChild(factionCard);
        factionElement.appendChild(membershipCard);

        messageArea.appendChild(factionElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }

    function showFaction(faction) {
        if (factionShow) {
            factionCard.src = './games/secrethitler/role-cover.png';
            factionShow = false;
        } else {
            if (faction === 'LIBERAL') {
                factionCard.src = './games/secrethitler/role-liberal.png';
            } else if (faction === 'FASCIST') {
                factionCard.src = './games/secrethitler/role-fascist.png';
            } else if (faction === 'HITLER') {
                factionCard.src = './games/secrethitler/role-hitler.png';
            }
            factionShow = true;
        }
    }

    function showMembership(faction) {
        if (membershipShow) {
            membershipCard.src = './games/secrethitler/membership-cover.png';
            membershipShow = false;
        } else {
            if (faction === 'LIBERAL') {
                membershipCard.src = './games/secrethitler/membership-liberal.png';
            } else {
                membershipCard.src = './games/secrethitler/membership-fascist.png';
            }
            membershipShow = true;
        }
    }

    function nominateChancellor(players) {
        showDialog('QUERY_CHANCELLOR', 'Nominate the chancellor for the government', players);
    }

    function vote(players) {
        showDialog('VOTE', 'Vote for the government:', players);
    }

    function showTopPolicies(policies) {
        bootbox.alert({
            closeButton: false,
            message: 'Peeked TOP policies: ' + policies,
            callback: function () {
                // TODO implement
            }
        });
    }

    function showInvestigatedFaction(faction) {
        bootbox.alert({
           closeButton: false,
           message: 'Faction of the investigated player: ' + faction,
           callback: function () {
               // TODO implement
           }
       });
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
                if (result === null) {
                    return false;
                } else {
                    sendReply(type, multiChoice ? result.join(',') : result);
                    return true;
                }
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
