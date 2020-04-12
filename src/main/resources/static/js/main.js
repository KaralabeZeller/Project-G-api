'use strict';

var usernamePage = document.querySelector('#username-page');
var usernameForm = document.querySelector('#usernameForm');
var startControllerButton = document.getElementById('startController');
var startScreenButton = document.getElementById('startScreen');

var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
		'#ff85af', '#FF9800', '#39bbb0' ];

var username = null;

var stringData = localStorage.getItem("name");
var obj = JSON.parse(stringData);

//TODO implement - url parameters for screen - lobby id and controller - username

function startController() {
    if(obj !== null) {
        username = obj;
    }
    else {
        username = document.getElementById('name').value.trim();
        console.log(username);
        localStorage.setItem("name",JSON.stringify(username));
    }
    window.location.href = './controller.html';
}

function startScreen() {
    window.location.href = './screen.html';
}

startControllerButton.onclick =  startController;
startScreenButton.onclick =  startScreen;

