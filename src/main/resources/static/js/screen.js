'use strict';
const canvasLiberal = document.getElementById('gameCanvasLiberal');
const ctxLiberal = canvasLiberal.getContext('2d');
const canvasFascist = document.getElementById('gameCanvasFascist');
const ctxFascist = canvasFascist.getContext('2d');

var stompClient = null;
var userList = new Array();

var liberalPolicies = 0;
var fascistPolicies = 0;

var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
		'#ff85af', '#FF9800', '#39bbb0' ];


connect();


function connect() {

    var socket = new SockJS('/ws');
    var sock_screen = new SockJS('/ws');
    stompClient = Stomp.over(sock_screen);

    stompClient.connect({}, onConnectedScreen, onError);


}

function onConnectedScreen() {
	stompClient.subscribe('/topic/public', onMessageReceivedScreen);
}

function onMessageReceivedScreen(payload) {
	var message = JSON.parse(payload.body);

    if (message.type === 'JOIN' || message.type === 'LEAVE') {

        var splitted = message.content.split(','), i;

        userList.length = 0;

        for (i = 0; i < splitted.length; i++) {
            userList.push(splitted[i]);
        }

    } else if (message.type === 'START') {
        playSecretHitler();
    } else if (message.type === 'GAME') {
        if (message.gameMessageType === 'FACTION') {
            displayFaction(message.content);
        } else {
          // TODO other messages
        }
    }
}

function onError(error) {
	console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}


function playSecretHitler() {
	drawBoards();

	setTimeout(() => { addLiberalPolicy(); addFascistPolicy();}, 1000);
	setTimeout(() => { addLiberalPolicy(); addFascistPolicy();}, 2000);
	setTimeout(() => { addLiberalPolicy(); addFascistPolicy();}, 3000);
	setTimeout(() => { addLiberalPolicy(); addFascistPolicy();}, 4000);
	setTimeout(() => { addLiberalPolicy(); addFascistPolicy();}, 5000);
	setTimeout(() => { addFascistPolicy(); }, 6000);

}

function addLiberalPolicy() {
	var drawing = new Image();
	drawing.src = "./games/secrethitler/liberalp-l.png";

	drawing.onload = function(){
		if(liberalPolicies == 0) {
			ctxLiberal.drawImage(this, 250, 160);
		}
		if(liberalPolicies == 1) {
			ctxLiberal.drawImage(this, 430, 160);
		}
		if(liberalPolicies == 2) {
			ctxLiberal.drawImage(this, 610, 160);
		}
		if(liberalPolicies == 3) {
			ctxLiberal.drawImage(this, 790, 160);
		}
		if(liberalPolicies == 4) {
			ctxLiberal.drawImage(this, 970, 160);
		}

		liberalPolicies ++;
	}

}

function addFascistPolicy() {
	var drawing = new Image();
	drawing.src = "./games/secrethitler/fascistp-l.png";

	drawing.onload = function(){
		if(fascistPolicies == 0) {
			ctxFascist.drawImage(this, 150, 160);
		}
		if(fascistPolicies == 1) {
			ctxFascist.drawImage(this, 330, 160);
		}
		if(fascistPolicies == 2) {
			ctxFascist.drawImage(this, 510, 160);
		}
		if(fascistPolicies == 3) {
			ctxFascist.drawImage(this, 690, 160);
		}
		if(fascistPolicies == 4) {
			ctxFascist.drawImage(this, 870, 160);
		}
		if(fascistPolicies == 5) {
			ctxFascist.drawImage(this, 1050, 160);
		}

		fascistPolicies ++;
	}
}

function drawBoards() {
	var drawing = new Image();
	var drawing2 = new Image();

	drawing.onload = function(){
	    var width = this.naturalWidth,
	        height = this.naturalHeight;

	    canvasLiberal.width = Math.floor(width / 2);
	    canvasLiberal.height = Math.floor(height / 2);

	    ctxLiberal.scale(0.5, 0.5);
	    ctxLiberal.drawImage(this, 0, 0);
	    ctxLiberal.scale(2,2);

	};
	drawing2.onload = function(){
	    var width = this.naturalWidth,
	        height = this.naturalHeight;

	    canvasFascist.width = Math.floor(width / 2);
	    canvasFascist.height = Math.floor(height / 2);

	    ctxFascist.scale(0.5, 0.5);
	    ctxFascist.drawImage(this, 0, 0);
	    ctxFascist.scale(2,2);
	};

	drawing.src = "./games/secrethitler/SH1.png";

	if(userList.length == 7 || userList.length == 7) {
		drawing2.src = "./games/secrethitler/SH2_2.png";
	} else if(userList.length == 9 || userList.length == 10) {
		drawing2.src = "./games/secrethitler/SH2_3.png";
	} else {
		drawing2.src = "./games/secrethitler/SH2.png";
	}
}


