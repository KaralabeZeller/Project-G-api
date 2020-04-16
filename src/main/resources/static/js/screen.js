(function () {
    'use strict';

    var splashScreen = document.getElementById('splash-page'), 
        playersArea = document.getElementById('playersArea'),
        canvasLiberal = document.getElementById('game-canvas-liberal'),
        canvasLiberalOverlay = document.getElementById('game-canvas-liberal-overlay'),
        canvasFascist = document.getElementById('game-canvas-fascist');        

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    // TODO implement - url parameters for screen - lobby id
    var userName = sessionStorage.getItem('name');
    var users = [];

    var president = null;
    var chancellor = null;
    var liberalPolicies = 0;
    var fascistPolicies = 0;

    connectScreen();

    function connectScreen() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        stompClient.subscribe('/topic/public', onMessageReceived);
        stompClient.subscribe('/user/topic/public', onMessageReceived);
    }

    function onError(error) {
        console.log('onError: Could not connect to WebSocket server. Please refresh this page to try again!', error);
    }

    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);
        var type = message.type;
        if (type === 'JOIN' || type === 'LEAVE') {
            var split = message.content.split(',');
            users.length = 0;
            users.push(...split);
        } else if (type === 'START') {
            playSecretHitler();
        } else if (type === 'GAME') {
            var gameType = message.gameType;
            if (gameType === 'PRESIDENT') {
                clearVotes();
                displayPresident(message.content);
            } else if (gameType === 'CHANCELLOR') {
                displayChancellor(message.content);
            } else if (gameType === 'VOTED') {
                vote(message.sender, message.content);
            }  else if (gameType === 'ENACTED_POLICY') {
                enactPolicy(message.content);
            } else {
              // TODO other messages
            }
        }
    }

    function playSecretHitler() {
        canvasLiberal.classList.remove('hidden');
        canvasLiberalOverlay.classList.remove('hidden');
        canvasFascist.classList.remove('hidden');
        playersArea.classList.remove('hidden');
        splashScreen.classList.add('hidden');

        drawBoards();
        moveTracker(0);
        users.forEach(drawPlayer);
    }
    
    function displayPresident(player) {
        if (president) {
            updatePlayer(president, '');
        }

        updatePlayer(player, 'PRESIDENT');
        president = player;
    }

    function displayChancellor(player) {
        if (chancellor) {
            updatePlayer(chancellor, '');
        }

        updatePlayer(player, 'CHANCELLOR');
        chancellor = player;
    }

    function vote(player, content) {
        updateLegend(player, content, '#FF0000');
    }
    
    function clearVotes() {
        if (chancellor) {
            updatePlayer(chancellor, '');
        }

        clearPlayerLabels

    }

    function clearPlayerLabels() {
        var index = 0;
        for (index; index < users.length; ++index) {
            var legend = document.getElementById('playerLegend-' + users[index]);
                legend.innerHTML = users[index];
                legend.style.color = '#000000';
        }
        if(chancellor !== null)
            updatePlayer(chancellor, '');
    }

    function drawPlayer(player) {
        var div = document.createElement('fieldset');
        div.id = 'fieldset-' + player;
        div.innerHTML =
            '<legend class="player-legend" id="playerLegend-' + player + '">' + player + '</legend>' +
            '<table><tbody><tr><td height="20" id="'+ player + '" class="player-role"></td></tr></tbody></table>';
        playersArea.appendChild(div);
    }
    
    function updatePlayer(player, text) {
        var div = document.getElementById(player);
        div.innerHTML = text;
    }
    
    function updateLegend(player, content, color) {
        var legend = document.getElementById('playerLegend-' + player);
        legend.innerHTML = player + ' - ' + content;
        legend.style.color = color;
    }

    function enactPolicy(policy) {
        if (policy === 'LIBERAL') {
            addLiberalPolicy();
        } else {
            addFascistPolicy();
        }
    }

    function addLiberalPolicy() {
        var ctxLiberal = canvasLiberal.getContext('2d');
        
        var drawing = new Image();
        drawing.onload = function() {
            if (liberalPolicies == 0) {
                ctxLiberal.drawImage(this, 250, 160);
            } else if (liberalPolicies == 1) {
                ctxLiberal.drawImage(this, 430, 160);
            } else if (liberalPolicies == 2) {
                ctxLiberal.drawImage(this, 610, 160);
            } else if (liberalPolicies == 3) {
                ctxLiberal.drawImage(this, 790, 160);
            } else if (liberalPolicies == 4) {
                ctxLiberal.drawImage(this, 970, 160);
            }
            liberalPolicies++;
        }
        drawing.src = './games/secrethitler/liberalp-l.png';
    }

    function addFascistPolicy() {
        var ctxFascist = canvasFascist.getContext('2d');
        
        var drawing = new Image();
        drawing.onload = function() {
            if (fascistPolicies == 0) {
                ctxFascist.drawImage(this, 150, 160);
            } else if (fascistPolicies == 1) {
                ctxFascist.drawImage(this, 330, 160);
            } else if (fascistPolicies == 2) {
                ctxFascist.drawImage(this, 510, 160);
            } else if (fascistPolicies == 3) {
                ctxFascist.drawImage(this, 690, 160);
            } else if (fascistPolicies == 4) {
                ctxFascist.drawImage(this, 870, 160);
            } else if (fascistPolicies == 5) {
                ctxFascist.drawImage(this, 1050, 160);
            }
            fascistPolicies++;
        }
        drawing.src = './games/secrethitler/fascistp-l.png';
    }

    function drawBoards() {
        var ctxLiberal = canvasLiberal.getContext('2d');
        var ctxFascist = canvasFascist.getContext('2d');

        var drawingLiberal = new Image();
        var drawingFascist = new Image();
        
        drawingLiberal.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasLiberal.width = Math.floor(width / 2);
            canvasLiberal.height = Math.floor(height / 2);

            ctxLiberal.scale(0.5, 0.5);
            ctxLiberal.drawImage(this, 0, 0);
            ctxLiberal.scale(2,2);
        };
        drawingFascist.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasFascist.width = Math.floor(width / 2);
            canvasFascist.height = Math.floor(height / 2);

            ctxFascist.scale(0.5, 0.5);
            ctxFascist.drawImage(this, 0, 0);
            ctxFascist.scale(2,2);
        };

        drawingLiberal.src = './games/secrethitler/SH1.png';

        if (users.length == 5 || users.length == 6) {
            drawingFascist.src = './games/secrethitler/SH2.png';
        } else if (users.length == 7 || users.length == 8) {
            drawingFascist.src = './games/secrethitler/SH2_2.png';
        } else if (users.length == 9 || users.length == 10) {
            drawingFascist.src = './games/secrethitler/SH2_3.png';
        } else {
            console.log('Failed to draw boards: Invalid user count: %s', users)
        }
    }

    //TODO implement
    function moveTracker(electionTracker) {
    }
}());
