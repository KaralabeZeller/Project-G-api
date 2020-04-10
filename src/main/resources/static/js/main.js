'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var startButton = document.getElementById('startButton');

const canvasLiberal = document.getElementById('gameCanvasLiberal');
const ctxLiberal = canvasLiberal.getContext('2d');
const canvasFascist = document.getElementById('gameCanvasFascist');
const ctxFascist = canvasFascist.getContext('2d');

var stompClient = null;
var stompClient_screen = null;
var username = null;
var userList = new Array();

var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
		'#ff85af', '#FF9800', '#39bbb0' ];

function connect(event) {
	username = document.querySelector('#name').value.trim();

	if (username) {
		usernamePage.classList.add('hidden');
		chatPage.classList.remove('hidden');

		var socket = new SockJS('/ws');
		var sock_screen = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient_screen = Stomp.over(sock_screen);

		stompClient.connect({}, onConnected, onError);
		stompClient_screen.connect({}, onConnectedScreen, onError);
	}
	event.preventDefault();

}

function onConnected() {
	// Subscribe to the Public Topic
	stompClient.subscribe('/topic/public', onMessageReceived);

	// Tell your username to the server
	stompClient.send("/app/chat.addUser", {}, JSON.stringify({
		sender : username,
		type : 'JOIN'
	}))

	connectingElement.classList.add('hidden');
}

function onConnectedScreen() {
	// Subscribe to the Public Topic
	stompClient_screen.subscribe('/topic/greetings', onMessageReceivedScreen);

	// Tell your username to the server
	stompClient.send("/app/hello", {}, JSON.stringify({
		'name' : username
	}));

	connectingElement.classList.add('hidden');
}

function onMessageReceivedScreen(payload) {
	// var message = payload;
	//
	// var messageElement = document.createElement('li');
	//
	//	
	// messageElement.classList.add('event-message');
	// message.content = 'Screen queue: ' + message;
	//
	//
	// var textElement = document.createElement('p');
	// var messageText = document.createTextNode(message.content);
	// textElement.appendChild(messageText);
	//
	// messageElement.appendChild(textElement);
	//
	// messageArea.appendChild(messageElement);
	// messageArea.scrollTop = messageArea.scrollHeight;
}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	connectingElement.style.color = 'red';
}

function sendMessage(event) {
	var messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		var chatMessage = {
			sender : username,
			content : messageInput.value,
			type : 'CHAT'
		};
		stompClient.send("/app/chat.sendMessage", {}, JSON
				.stringify(chatMessage));
		messageInput.value = '';
	}
	event.preventDefault();
}

function startGame(event) {
	var chatMessage = {
		sender : username,
		content : 'SecretHitler',
		type : 'START'
	};
	stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

	event.preventDefault();
}

function onMessageReceived(payload) {
	var message = JSON.parse(payload.body);

	if (message.type === 'JOIN' || message.type === 'LEAVE') {

		if (message.type === 'JOIN') {
			userList.push(message.sender)
		} else {
			if (message.type === 'JOIN') {
				userList.remove(message.sender)
			}
		}

		if (userList.length >= 2) {
			startButton.classList.remove('hidden');
		} else {
			startButton.classList.add('hidden');
		}

		var splitted = message.content.split(','), i;

		var element = document.getElementById('messageArea');
		element.innerHTML = '';

		for (i = 0; i < splitted.length; i++) {

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
			message.content = '';

			var textElement = document.createElement('p');
			var messageText = document.createTextNode('');
			textElement.appendChild(messageText);

			messageElement.appendChild(textElement);

			messageArea.appendChild(messageElement);
			messageArea.scrollTop = messageArea.scrollHeight;
		}

	} else if (message.type === 'START') {
		playSecretHitler();
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
	
	drawBoards();
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
	drawing2.src = "./games/secrethitler/SH2.png";
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', startGame, true)
