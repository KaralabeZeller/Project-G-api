(function () {
    'use strict';

    var canvasLiberal = document.getElementById('gameCanvasLiberal'),
        ctxLiberal = canvasLiberal.getContext('2d'),
        canvasFascist = document.getElementById('gameCanvasFascist'),
        ctxFascist = canvasFascist.getContext('2d'),
        playersDiv = document.getElementById('players'),
        splashScreen = document.getElementById('splashScreen');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    var stompClient = null;

    // TODO implement - url parameters for screen - lobby id and controller - username
    var username = sessionStorage.getItem('name');
    var users = [];

    var president = null;
    var chancellor = null;
    var liberalPolicies = 0;
    var fascistPolicies = 0;

    canvasLiberal.classList.add('hidden');
    canvasFascist.classList.add('hidden');
    playersDiv.classList.add('hidden');

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
        if (message.type === 'JOIN' || message.type === 'LEAVE') {
            var split = message.content.split(',');
            users.length = 0;
            users.push(...split);
        } else if (message.type === 'START') {
            initScreen();
            playSecretHitler();
        } else if (message.type === 'GAME') {
            if (message.gameMessageType === 'PRESIDENT') {
                setPresident(message.content);
            } else if (message.gameMessageType === 'CHANCELLOR') {
                setChancellor(message.content);
            } else if (message.gameMessageType === 'VOTED') {
                vote(message.sender, message.content);
            }else {
              // TODO other messages
            }
        }
    }

    function initScreen() {
        canvasLiberal.classList.remove('hidden');
        canvasFascist.classList.remove('hidden');
        playersDiv.classList.remove('hidden');
        splashScreen.classList.add('hidden');
    }

    function playSecretHitler() {
        drawBoards();
        users.forEach(drawPlayer);

        setTimeout(() => { addLiberalPolicy(); addFascistPolicy(); }, 1000);
        setTimeout(() => { addLiberalPolicy(); addFascistPolicy(); }, 2000);
        setTimeout(() => { addLiberalPolicy(); addFascistPolicy(); }, 3000);
        setTimeout(() => { addLiberalPolicy(); addFascistPolicy(); }, 4000);
        setTimeout(() => { addLiberalPolicy(); addFascistPolicy(); }, 5000);
        setTimeout(() => { addFascistPolicy(); }, 6000);
    }
    
    function setPresident(player) {
        if (president) {
            updatePlayer(president, "");
        }
        updatePlayer(player, "PRESIDENT");
        president = player;
    }

    function setChancellor(player) {
        if (chancellor) {
            updatePlayer(chancellor, "");
        }

        updatePlayer(player, "CHANCELLOR");
        chancellor = player;
    }

    function vote(sender, content) {
        var playerName = document.getElementById('playerLegend'+sender);
        playerName.innerHTML = sender + ' - ' + content;
        playerName.style.color="#FF0000";
    }
   
    function drawPlayer(player) {
        var div = document.createElement('fieldset');
        div.id = 'fieldset-' + player;
        div.innerHTML =
            '<legend class="player-legend" id="playerLegend'+player+'">' + player + '</legend>' +
            '<table><tbody><tr><td height="20" id="'+ player + '" class="player-role"></td></tr></tbody></table>';
        playersDiv.appendChild(div);
    }
    
    function updatePlayer(player, text) {
        var div = document.getElementById(player);
        div.innerHTML = text;
    }

    function addLiberalPolicy() {
        var drawing = new Image();
        
        drawing.onload = function() {
            if (liberalPolicies == 0) {
                ctxLiberal.drawImage(this, 250, 160);
            }
            if (liberalPolicies == 1) {
                ctxLiberal.drawImage(this, 430, 160);
            }
            if (liberalPolicies == 2) {
                ctxLiberal.drawImage(this, 610, 160);
            }
            if (liberalPolicies == 3) {
                ctxLiberal.drawImage(this, 790, 160);
            }
            if (liberalPolicies == 4) {
                ctxLiberal.drawImage(this, 970, 160);
            }

            liberalPolicies ++;
        }
        
        drawing.src = "./games/secrethitler/liberalp-l.png";
    }

    function addFascistPolicy() {
        var drawing = new Image();

        drawing.onload = function() {
            if (fascistPolicies == 0) {
                ctxFascist.drawImage(this, 150, 160);
            }
            if (fascistPolicies == 1) {
                ctxFascist.drawImage(this, 330, 160);
            }
            if (fascistPolicies == 2) {
                ctxFascist.drawImage(this, 510, 160);
            }
            if (fascistPolicies == 3) {
                ctxFascist.drawImage(this, 690, 160);
            }
            if (fascistPolicies == 4) {
                ctxFascist.drawImage(this, 870, 160);
            }
            if (fascistPolicies == 5) {
                ctxFascist.drawImage(this, 1050, 160);
            }

            fascistPolicies ++;
        }
        
        drawing.src = "./games/secrethitler/fascistp-l.png";
    }

    function drawBoards() {
        var drawing = new Image();
        var drawing2 = new Image();

        drawing.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasLiberal.width = Math.floor(width / 2);
            canvasLiberal.height = Math.floor(height / 2);

            ctxLiberal.scale(0.5, 0.5);
            ctxLiberal.drawImage(this, 0, 0);
            ctxLiberal.scale(2,2);

        };
        drawing2.onload = function() {
            var width = this.naturalWidth,
                height = this.naturalHeight;

            canvasFascist.width = Math.floor(width / 2);
            canvasFascist.height = Math.floor(height / 2);

            ctxFascist.scale(0.5, 0.5);
            ctxFascist.drawImage(this, 0, 0);
            ctxFascist.scale(2,2);
        };

        drawing.src = "./games/secrethitler/SH1.png";

        if (users.length == 7 || users.length == 8) {
            drawing2.src = "./games/secrethitler/SH2_2.png";
        } else if (users.length == 9 || users.length == 10) {
            drawing2.src = "./games/secrethitler/SH2_3.png";
        } else {
            drawing2.src = "./games/secrethitler/SH2.png";
        }
    }

}());
