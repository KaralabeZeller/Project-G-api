(function () {
    'use strict';

    var startControllerButton = document.getElementById('startController'),
        startScreenButton = document.getElementById('startScreen');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107',
            '#ff85af', '#FF9800', '#39bbb0' ];

    //TODO implement - url parameters for screen - lobby id and controller - username
    var username = null;
    var stringData = sessionStorage.getItem('name');
    var obj = JSON.parse(stringData);
    if (obj !== null) {
        username = obj;
    }

    function startController() {
        if (username === null) {
            username = document.getElementById('name').value.trim();
            sessionStorage.setItem('name', JSON.stringify(username));
        }
        window.location.href = './controller.html';
    }

    function startScreen() {
        window.location.href = './screen.html';
    }

    startControllerButton.onclick = startController;
    startScreenButton.onclick = startScreen;

}());
