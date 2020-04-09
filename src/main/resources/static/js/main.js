'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

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

function onMessageReceived(payload) {
	var message = JSON.parse(payload.body);

	if (message.type === 'JOIN') {
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

	} else if (message.type === 'LEAVE') {
		var messageElement = document.createElement('li');

		messageElement.classList.add('event-message');
		message.content = message.sender + ' left!';

		var textElement = document.createElement('p');
		var messageText = document.createTextNode(message.content);
		textElement.appendChild(messageText);

		messageElement.appendChild(textElement);

		messageArea.appendChild(messageElement);
		messageArea.scrollTop = messageArea.scrollHeight;

	} else {

		var messageElement = document.createElement('li');
		messageElement.classList.add('chat-message');

		var avatarElement = document.createElement('i');
		var avatarText = document.createTextNode(message.sender[0]);
		avatarElement.appendChild(avatarText);
		avatarElement.style['background-color'] = getAvatarColor(message.sender);

		messageElement.appendChild(avatarElement);

		var usernameElement = document.createElement('span');
		var usernameText = document.createTextNode(message.sender);
		usernameElement.appendChild(usernameText);
		messageElement.appendChild(usernameElement);

		var textElement = document.createElement('p');
		var messageText = document.createTextNode(message.content);
		textElement.appendChild(messageText);

		messageElement.appendChild(textElement);

		messageArea.appendChild(messageElement);
		messageArea.scrollTop = messageArea.scrollHeight;

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

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)