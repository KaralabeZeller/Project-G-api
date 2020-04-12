'use strict';

var chatPage = document.querySelector('#chat-page');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var lobbyText =document.getElementById("LobbyText");

var stompClient = null;
var stompClient_screen = null;
var username = null;
var userList = new Array();

var liberalPolicies = 0;
var fascistPolicies = 0;

var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
		'#ff85af', '#FF9800', '#39bbb0' ];


var stringData = localStorage.getItem("name");
var obj = JSON.parse(stringData);

connect();

if(obj !== null) {
    username = obj;
    chatPage.classList.remove('hidden');

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
    localStorage.setItem("name",JSON.stringify(username));
}

function connect() {

	if (username) {
		usernamePage.classList.add('hidden');
		chatPage.classList.remove('hidden');

		var socket = new SockJS('/ws');
		var sock_screen = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient_screen = Stomp.over(sock_screen);

		stompClient.connect({}, onConnected, onError);
		stompClient_screen.connect({}, onConnectedScreen, onError);
        localStorage.setItem("name",JSON.stringify(username));

    }

}

function onConnected() {
	// Subscribe to the Public Topic
	stompClient.subscribe('/topic/public', onMessageReceived);
	stompClient.subscribe('/user/topic/public', onMessageReceived);

	// Tell your username to the server
	stompClient.send("/app/chat.addUser", {}, JSON.stringify({
		sender : username,
		type : 'JOIN'
	}))

	connectingElement.classList.add('hidden');
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

		var splitted = message.content.split(','), i;

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
			message.content = '';

			var textElement = document.createElement('p');
			var messageText = document.createTextNode('');
			textElement.appendChild(messageText);

			messageElement.appendChild(textElement);

			messageArea.appendChild(messageElement);
			messageArea.scrollTop = messageArea.scrollHeight;
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
	    } else {
	      // TODO other messages
	    }
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
