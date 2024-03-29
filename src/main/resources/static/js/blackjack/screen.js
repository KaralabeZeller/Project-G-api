(function () {
    'use strict';

    var splashScreen  = document.getElementById('splash-page'),
        rules         = document.getElementById('rules'),
        playersArea   = document.getElementById('playersArea'),
        pokerBoard    = document.getElementById('poker-board'),
        statusBar     = document.getElementById('statusBar'),
        statusBarText = document.getElementById('statusBarText'),
        qrCode        = document.getElementById("qrCode");

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    // TODO use url parameters for controller (lobby id, username)
    var userName = document.getElementById('userName').value;
    var lobbyName = document.getElementById('lobbyName').value;

    var stompClient;
    var subscriptionLobby;
    var subscriptionPublic;
//    var subscriptionUser;

    var users = [];


    connectScreen();

    function connectScreen() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        subscriptionLobby = stompClient.subscribe('/topic/lobby/' + lobbyName, onMessageReceived);
        subscriptionPublic = stompClient.subscribe('/topic/game/' + lobbyName, onMessageReceived);

        statusText('Scan the QR on your phone to join the game');
        generateQR();
    }

    function onError(error) {
        console.log('onError: Could not connect to WebSocket server. Please refresh this page to try again!', error);
    }

    function disconnectScreen() {
        subscriptionLobby.unsubscribe();
        subscriptionPublic.unsubscribe();
//        subscriptionUser.unsubscribe();

        stompClient.disconnect(onDisconnected);
    }

    function onDisconnected() {
        console.log('DISCONNECTED');
    }

    function statusText(text) {
        statusBarText.classList.add('hide');
        setTimeout(function () {
            statusBarText.innerHTML = text;
            statusBarText.classList.remove('hide');
        }, 500);
    }

    function generateQR() {
        qrCode.src = "https://qrickit.com/api/qr.php?d=http://api.project-g.xyz:8080/join/" + lobbyName  ;
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        var type = message.type;
        if (type === 'LOBBY') {
            var lobbyType = message.lobbyType;
            if (lobbyType === 'JOIN' || lobbyType === 'LEAVE') {
                var split = message.content.split(',');
                users.length = 0;
                users.push(...split);
            }
        } else if (type === 'START') {
            subscriptionLobby.unsubscribe();
            playBlackJack();
        } else if (type === 'GAME') {
            var gameType = message.gameType;
            if (gameType === 'SCREEN') {
               updatePlayer(message.content);
            } else {
                console.log('Ignoring game message: %s', message);
            }
        } else if (type === 'STOP') {
            disconnectScreen();
        } else {
            console.log('Ignoring other message: %s', message);
        }
    }

    function updatePlayer(message) {

        var split = message.split(': ');
        var player = split[0];
        var cards = split[1].split(',');

        console.log("Player %s is dealt %s and %s", player, cards[0], cards[1]);

        var cardName1 =  '/games/common/cards/poker/' + cards[0] + '.svg';
        var cardName2 =  '/games/common/cards/poker/' + cards[1] + '.svg';

        var div1 = document.getElementById('card1-' + player);
        var output = '<img src="' + cardName1 + '" height="60" />';
        div1.innerHTML = output;

        var div2 = document.getElementById('card2-' + player);
        var output2 = '<img src="' + cardName2 + '" height="60" />';
        div2.innerHTML = output2;
    }
    
    function playBlackJack() {
            
        pokerBoard.classList.remove('hidden');
        playersArea.classList.remove('hidden');
        statusBar.classList.remove('hidden');
        splashScreen.classList.add('hidden');
        rules.classList.add('hidden');

        qrCode.classList.add('hidden');

        users.forEach(drawPlayer);

        drawPokerBoard();
    }
        
    function drawPokerBoard() {
        var ctxBoard = pokerBoard.getContext('2d');

        var drawingBoard = new Image();

        drawingBoard.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            pokerBoard.width = Math.floor(width / 2);
            pokerBoard.height = Math.floor(height / 2);

            ctxBoard.scale(0.5, 0.5);
            ctxBoard.drawImage(this, 0, 0);
            ctxBoard.scale(2,2);
        };


        drawingBoard.src = '/games/blackjack/felt.jpg';
    
    }

    function drawPlayer(player) {
        var div = document.createElement('div');
        var table = document.createElement('table');
        var body = document.createElement('tbody');
        var row = document.createElement('tr');
        var avatar = document.createElement('td');
        var playerName = document.createElement('td');
        var card1 = document.createElement('td');
        var card2 = document.createElement('td');
        var color = '#c9ccce';

        table.id = 'table-' + player;
        div.id = 'fieldset-' + player;
        div.style.backgroundColor = getRandomColor(color);
        playerName.innerHTML = player;
        playerName.id = 'name-' + player;
        playerName.width = '150 px';
        playerName.style.fontWeight = 'bold';
        card1.id = 'card1-' + player;
        card2.id = 'card2-' + player;
        avatar.id = 'avatar-' + player;
        avatar.innerHTML = '<img src="/games/secrethitler/lizard.png" width="60" height="60" />';

        row.appendChild(avatar);
        row.appendChild(playerName);
        row.appendChild(card1);
        row.appendChild(card2);
        body.appendChild(row);
        table.appendChild(body);
        div.appendChild(table);
        playersArea.appendChild(div);
    }
    function getRandomColor(color) {
        var p = 1,
            temp,
            random = Math.random(),
            result = '#';

        while (p < color.length) {
            temp = parseInt(color.slice(p, p += 2), 16)
            temp += Math.floor((255 - temp) * random);
            result += temp.toString(16).padStart(2, '0');
        }
        return result;
    }

}());
