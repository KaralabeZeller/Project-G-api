(function () {
    'use strict';

    var userNameInput = document.getElementById('userName'),
        startControllerButton = document.getElementById('startController'),
        startScreenButton = document.getElementById('startScreen');

    var colors = [ '#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0' ];

    // TODO use url parameters for screen (lobby id) and controller (lobby id, username)
    var userName = sessionStorage.getItem('name');

    function startController() {
        if (!userName) {
            userName = userNameInput.value.trim();
            if (!userName) {
                // TODO use bootbox.alert (see bootbox.prompt in controller)
                alert('Please provide a username');
            } else {
                sessionStorage.setItem('name', userName);
            }
        }
        window.location.href = './controller.html';
    }

    function startScreen() {
        window.location.href = './screen.html';
    }

    startControllerButton.onclick = startController;
    startScreenButton.onclick = startScreen;

}());
