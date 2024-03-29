(function () {
    'use strict';

    var splashScreen  = document.getElementById('splash-page'),
        splashVictory = document.getElementById('splash-victory'),
        rules         = document.getElementById('rules'),
        playersArea   = document.getElementById('playersArea'),
        canvasLiberal = document.getElementById('game-canvas-liberal'),
        canvasFascist = document.getElementById('game-canvas-fascist'),
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

    var president = null;
    var chancellor = null;
    var liberalPolicies = 0;
    var fascistPolicies = 0;
    var tracker = '0';


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
            playSecretHitler();
        } else if (type === 'GAME') {
            var gameType = message.gameType;
            if (gameType === 'PRESIDENT') {
                setTimeout(clearPlayerVotes, 30000);
                displayPresident(message.content);
            } else if (gameType === 'CHANCELLOR') {
                displayChancellor(message.content);
            } else if (gameType === 'VOTED') {
                vote(message.sender, message.content);
            }  else if (gameType === 'ENACTED_POLICY') {
                enactPolicy(message.content);
            }  else if (gameType === 'TRACKER') {
                moveTracker(message.content);
            } else if (gameType === 'KILLED') {
                killUser(message.content);
            } else if (gameType === 'STATE') {
                statusText(message.content);
            } else if (gameType === 'VICTORY') {
                processVictory(message.content);
            } else {
                console.log('Ignoring game message: %s', message);
            }
        } else if (type === 'STOP') {
            disconnectScreen();
        } else {
            console.log('Ignoring other message: %s', message);
        }
    }

    function playSecretHitler() {
        canvasLiberal.classList.remove('hidden');
        canvasFascist.classList.remove('hidden');
        playersArea.classList.remove('hidden');
        statusBar.classList.remove('hidden');
        splashScreen.classList.add('hidden');
        rules.classList.add('hidden');
        splashVictory.classList.add('hidden');
        qrCode.classList.add('hidden');

        drawLiberalBoard();
        drawFascistBoard();

        users.forEach(drawPlayer);
    }
    
    function displayPresident(player) {
        if (president) {
            updatePlayer(president, '');
        }
        if (chancellor) {
             updatePlayer(chancellor, '');
        }

        updatePlayer(player, 'PRESIDENT');
        president = player;
    }

    function displayChancellor(player) {
        if (chancellor && chancellor !== president) {
            updatePlayer(chancellor, '');
        }

        updatePlayer(player, 'CHANCELLOR');
        chancellor = player;
    }

    function vote(player, content) {
        updateVote(player, content);
    }

    function clearPlayerVotes() {
        users.forEach(player => updateVote(player, null));
    }

    function killUser(player) {
        var avatar = document.getElementById('avatar-' + player),
            div = document.getElementById('fieldset-' + player),
            name = document.getElementById('name-' + player);

        div.style.backgroundColor = '#c9ccce'; // TODO stylesheet
        avatar.innerHTML = '<img src="/games/secrethitler/lizard_killed.png" width="60" height="60" />';
        name.style.color = 'grey';
    }

    function drawPlayer(player) {
        var div = document.createElement('div');
        var table = document.createElement('table');
        var body = document.createElement('tbody');
        var row = document.createElement('tr');
        var avatar = document.createElement('td');
        var playerName = document.createElement('td');
        var playerRole = document.createElement('td');
        var playerVote = document.createElement('td');
        var color = '#c9ccce';

        table.id = 'table-' + player;
        div.id = 'fieldset-' + player;
        div.style.backgroundColor = getRandomColor(color);
        playerName.innerHTML = player;
        playerName.id = 'name-' + player;
        playerName.width = '150 px';
        playerName.style.fontWeight = 'bold';
        playerRole.id = player;
        playerVote.id = 'playerVote-' + player;
        playerRole.width = '230 px';
        avatar.id = 'avatar-' + player;
        avatar.innerHTML = '<img src="/games/secrethitler/lizard.png" width="60" height="60" />';

        row.appendChild(avatar);
        row.appendChild(playerName);
        row.appendChild(playerRole);
        row.appendChild(playerVote);
        body.appendChild(row);
        table.appendChild(body);
        div.appendChild(table);
        playersArea.appendChild(div);
    }
    
    function updatePlayer(player, text) {
        var div = document.getElementById(player);
        var output = text;
        if (text === 'PRESIDENT') {
            output = '<img src="/games/secrethitler/president.png" height="60" />';
        } else if (text === 'CHANCELLOR') {
            output = '<img src="/games/secrethitler/chancellor.png" height="60" />';
        }
        div.innerHTML = output;
    }

    function updateVote(player, content) {
        var div = document.getElementById('playerVote-' + player);
        var output = '';
        if (content === 'Ja!') {
            output = '<img src="/games/secrethitler/ballot-ja.png" height="60" />';
        } else if (content === 'Nein!') {
            output = '<img src="/games/secrethitler/ballot-no.png" height="60" />';
        } else {
           output = '';
        }
        setTimeout(function () {
            div.innerHTML = output;
            div.classList.remove('hide');
        }, 500);
    }

    function enactPolicy(policy) {
        if (policy === 'LIBERAL') {
            addLiberalPolicy();
        } else {
            addFascistPolicy();
        }
    }

    function moveTracker(track) {
        tracker = track;

        drawLiberalBoard();
        setTimeout(redrawLiberal, 100);

    }

    function addLiberalPolicy() {
        var ctxLiberal = canvasLiberal.getContext('2d');
        
        var drawing = new Image();
        drawing.onload = function() {
            if (liberalPolicies === 0) {
                ctxLiberal.drawImage(this, 250, 160);
            } else if (liberalPolicies === 1) {
                ctxLiberal.drawImage(this, 430, 160);
            } else if (liberalPolicies === 2) {
                ctxLiberal.drawImage(this, 610, 160);
            } else if (liberalPolicies === 3) {
                ctxLiberal.drawImage(this, 790, 160);
            } else if (liberalPolicies === 4) {
                ctxLiberal.drawImage(this, 970, 160);
            }
            liberalPolicies++;
        }
        drawing.src = '/games/secrethitler/liberalp-l.png';
    }

    function redrawLiberal() {
        var ctxLiberal = canvasLiberal.getContext('2d');

        var drawing = new Image();
        var i = 0;

        drawing.onload = function() {
            for (i; i <=liberalPolicies; i++) {
                if (i === 1) {
                    ctxLiberal.drawImage(this, 250, 160);
                } else if (i === 2) {
                    ctxLiberal.drawImage(this, 430, 160);
                } else if (i === 3) {
                    ctxLiberal.drawImage(this, 610, 160);
                } else if (i === 4) {
                    ctxLiberal.drawImage(this, 790, 160);
                } else if (i === 5) {
                    ctxLiberal.drawImage(this, 970, 160);
                }
            }
        }
        drawing.src = '/games/secrethitler/liberalp-l.png';
    }

    function addFascistPolicy() {
        var ctxFascist = canvasFascist.getContext('2d');
        
        var drawing = new Image();
        drawing.onload = function() {
            if (fascistPolicies === 0) {
                ctxFascist.drawImage(this, 150, 160);
            } else if (fascistPolicies === 1) {
                ctxFascist.drawImage(this, 330, 160);
            } else if (fascistPolicies === 2) {
                ctxFascist.drawImage(this, 510, 160);
            } else if (fascistPolicies === 3) {
                ctxFascist.drawImage(this, 690, 160);
            } else if (fascistPolicies === 4) {
                ctxFascist.drawImage(this, 870, 160);
            } else if (fascistPolicies === 5) {
                ctxFascist.drawImage(this, 1050, 160);
            }
            fascistPolicies++;
        }
        drawing.src = '/games/secrethitler/fascistp-l.png';
    }
    
    function processVictory(faction) {
       if (faction === 'LIBERAL') {
            splashVictory.src='/games/secrethitler/SH1_liberals_won.png';
       } else {
            splashVictory.src='/games/secrethitler/SH2_fascists_won.png';
       }
       splashVictory.classList.remove('hidden');
    }

    function drawFascistBoard() {
        var ctxFascist = canvasFascist.getContext('2d');

        var drawingFascist = new Image();

        drawingFascist.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasFascist.width = Math.floor(width / 2);
            canvasFascist.height = Math.floor(height / 2);

            ctxFascist.scale(0.5, 0.5);
            ctxFascist.drawImage(this, 0, 0);
            ctxFascist.scale(2,2);
        };

        if (users.length === 5 || users.length === 6) {
            drawingFascist.src = '/games/secrethitler/SH2.png';
        } else if (users.length === 7 || users.length === 8) {
            drawingFascist.src = '/games/secrethitler/SH2_2.png';
        } else if (users.length === 9 || users.length === 10) {
            drawingFascist.src = '/games/secrethitler/SH2_3.png';
        } else {
            console.log('Failed to draw boards: Invalid user count: %s', users)
        }

    }

    function drawLiberalBoard() {
        var ctxLiberal = canvasLiberal.getContext('2d');

        var drawingLiberal = new Image();

        drawingLiberal.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasLiberal.width = Math.floor(width / 2);
            canvasLiberal.height = Math.floor(height / 2);

            ctxLiberal.scale(0.5, 0.5);
            ctxLiberal.drawImage(this, 0, 0);
            ctxLiberal.scale(2,2);
        };

        if (tracker === '0') {
            drawingLiberal.src = '/games/secrethitler/SH1_0.png';
        } else if (tracker === '1') {
            drawingLiberal.src = '/games/secrethitler/SH1_1.png';
        } else if (tracker === '2') {
            drawingLiberal.src = '/games/secrethitler/SH1_2.png';
        } else if (tracker === '3') {
            drawingLiberal.src = '/games/secrethitler/SH1_3.png';
        }

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
