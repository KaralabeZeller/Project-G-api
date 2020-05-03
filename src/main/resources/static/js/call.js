function setupCall(_lobbyName, _send) {
    'use strict';

    const lobbyName = _lobbyName
    const send = _send;
    
    const startButton = document.getElementById('startButton');
    const callButton = document.getElementById('callButton');
    const hangupButton = document.getElementById('hangupButton');

    callButton.disabled = true;
    hangupButton.disabled = true;
    startButton.onclick = start;
    callButton.onclick = call;
    hangupButton.onclick = hangup;
    
    const servers = {
        iceServers: [
//            { urls: 'stun:stun.l.google.com:19302' },
//            { urls: 'stun:stun01.sipphone.com' },
//            { urls: 'stun:stun.ekiga.net' },
//            { urls: 'stun:stun.fwdnet.net' },
//            { urls: 'stun:stun.ideasip.com' },
//            { urls: 'stun:stun.iptel.org' },
//            { urls: 'stun:stun.rixtelecom.se' },
//            { urls: 'stun:stun.schlund.de' },
           { urls: 'stun:stun.l.google.com:19302' },
           { urls: 'stun:stun1.l.google.com:19302' },
           { urls: 'stun:stun2.l.google.com:19302' },
           { urls: 'stun:stun3.l.google.com:19302' },
           { urls: 'stun:stun4.l.google.com:19302' },
//            { urls: 'stun:stunserver.org' },
//            { urls: 'stun:stun.softjoys.com' },
//            { urls: 'stun:stun.voiparound.com' },
//            { urls: 'stun:stun.voipbuster.com' },
//            { urls: 'stun:stun.voipstunt.com' },
//            { urls: 'stun:stun.voxgratia.org' },
//            { urls: 'stun:stun.xten.com' }
        ]
    };

    const video1 = document.querySelector('video#video1');
    const video2 = document.querySelector('video#video2');

    let peer;

    const mediaOptions = {
        audio: true,
        video: true,
    };

    const offerOptions = {
        offerToReceiveAudio: 1,
        offerToReceiveVideo: 1,
    };

    const answerOptions = {
    };

    // COMMON

    function start() {
        startButton.disabled = true;

        console.log('start: Requesting local stream');
        navigator.mediaDevices
            .getUserMedia(mediaOptions)
            .then(onLocalStream)
            .catch(e => console.log('getUserMedia() error: ', e));

        console.log('start: Creating peer connection object');
        peer = new RTCPeerConnection(servers);
        peer.ontrack = onRemoteStream;
        peer.onicecandidate = sendCandidate;
        console.log('start: Created peer connection object');
    }

    function onLocalStream(stream) {
        callButton.disabled = false;

        if (video1.srcObject !== stream) {
            console.log('onLocalStream: Received local stream: %s', stream);
            video1.srcObject = stream;
            window.localStream = stream;
        }
    }
    
    function onRemoteStream(e) {
        var stream = e.streams[0];
        if (video2.srcObject !== stream) {
            console.log('onRemoteStream: Received remote stream: %s', stream);
            video2.srcObject = stream;
        }
    }

    function startStream() {
        callButton.disabled = true;
        hangupButton.disabled = false;

        console.log('call: Enumerating local stream tracks');
        const audioTracks = window.localStream.getAudioTracks();
        const videoTracks = window.localStream.getVideoTracks();
        if (audioTracks.length > 0) {
            console.log(`Using audio device: ${audioTracks[0].label}`);
        }
        if (videoTracks.length > 0) {
            console.log(`Using video device: ${videoTracks[0].label}`);
        }

        console.log('call: Adding local stream tracks to peer connection');
        window.localStream.getTracks().forEach(track => peer.addTrack(track, window.localStream));
    }
    
    function hangup() {
        console.log('hangup: Closing peer connection object');
        peer.close();
        peer = null;
        console.log('hangup: Closed peer connection object');

        hangupButton.disabled = true;
        callButton.disabled = false;
    }

    // CALLER

    function onError(error) {
        console.log(`Failed to process call: ${error.toString()}`, error);
    }

    function call() {
        startStream();

        createOffer();
    }

    function createOffer() {
        console.log(`createOffer:\n${offerOptions}`);
        peer.createOffer(sendOffer, onError, offerOptions);
    }
    
    function sendOffer(desc) {
        console.log(`sendOffer:\n${desc.sdp}`);
        peer.setLocalDescription(desc, () => {}, onError);
        send('OFFER', desc);
    }

    function onAnswer(desc) {
        console.log(`onAnswer:\n${desc.sdp}`);
        peer.setRemoteDescription(desc, () => {}, onError);
    }

    // RECEIVER
    
    function onOffer(desc) {
        startStream();

        console.log(`onOffer:\n${desc.sdp}`);
        peer.setRemoteDescription(desc, createAnswer, onError);
    }

    function createAnswer() {
        console.log(`createAnswer:\n${answerOptions}`);
        peer.createAnswer(sendAnswer, onError, answerOptions);
    }

    function sendAnswer(desc) {
        console.log(`sendAnswer:\n${desc.sdp}`);
        peer.setLocalDescription(desc, () => {}, onError);
        send('ANSWER', desc);
    }

    // ICE - STUN, TURN

    function onCandidate(desc) {
        var candidate = new RTCIceCandidate(desc);
        console.log(`Adding ICE candidate: ${candidate}`);
        peer.addIceCandidate(candidate, onAddIceCandidateSuccess, onAddIceCandidateError);
    }

    function sendCandidate(event) {
        var candidate = event.candidate;
        if (candidate) {
            console.log(`Sending ICE candidate: ${candidate}`);
            send('CANDIDATE', candidate);
        }
    }

    function onAddIceCandidateSuccess(candidate) {
        console.log(`Added ICE candidate: ${candidate ? candidate : '(null)'}`);
    }

    function onAddIceCandidateError(error) {
        console.log(`Failed to add ICE candidate: ${error.toString()}`, error);
    }

    // Return object with public functions
    return {
        onOffer: onOffer,
        onAnswer: onAnswer,
        onCandidate: onCandidate,
    };
}
