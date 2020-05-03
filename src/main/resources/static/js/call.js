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
           { urls: 'stun:stun.l.google.com:19302' },
           { urls: 'stun:stun01.sipphone.com' },
           { urls: 'stun:stun.ekiga.net' },
           { urls: 'stun:stun.fwdnet.net' },
           { urls: 'stun:stun.ideasip.com' },
           { urls: 'stun:stun.iptel.org' },
           { urls: 'stun:stun.rixtelecom.se' },
           { urls: 'stun:stun.schlund.de' },
           { urls: 'stun:stun.l.google.com:19302' },
           { urls: 'stun:stun1.l.google.com:19302' },
           { urls: 'stun:stun2.l.google.com:19302' },
           { urls: 'stun:stun3.l.google.com:19302' },
           { urls: 'stun:stun4.l.google.com:19302' },
           { urls: 'stun:stunserver.org' },
           { urls: 'stun:stun.softjoys.com' },
           { urls: 'stun:stun.voiparound.com' },
           { urls: 'stun:stun.voipbuster.com' },
           { urls: 'stun:stun.voipstunt.com' },
           { urls: 'stun:stun.voxgratia.org' },
           { urls: 'stun:stun.xten.com' }
        ]
    };

    const video1 = document.querySelector('video#video1');
    const video2 = document.querySelector('video#video2');

    let peerLocal;
    let peerRemote;

    const mediaOptions = {
        audio: true,
        video: false,    
    };

    const offerOptions = {
        offerToReceiveAudio: 1,
        offerToReceiveVideo: 0,
    };

    const answerOptions = offerOptions;

    // COMMON

    function start() {
        startButton.disabled = true;

        console.log('start: Requesting local stream');
        navigator.mediaDevices
            .getUserMedia(mediaOptions)
            .then(onLocalStream)
            .catch(e => console.log('getUserMedia() error: ', e));

        peerLocal = new RTCPeerConnection(servers);
        peerRemote = new RTCPeerConnection(servers);
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
        if (video2.srcObject !== e.streams[0]) {
            console.log('onRemoteStream: Received remote stream: %s', stream);
            video2.srcObject = e.streams[0];
        }
    }    
    
    // TODO other way around
    // CALLER - peerLocal
    
    function onError(error) {
        console.log(`Failed to process call: ${error.toString()}`, error);
    }        

    function call() {
        callButton.disabled = true;
        hangupButton.disabled = false;

        console.log('call: Creating local and remote peer connection objects');
        const audioTracks = window.localStream.getAudioTracks();
        const videoTracks = window.localStream.getVideoTracks();
        if (audioTracks.length > 0) {
            console.log(`Using audio device: ${audioTracks[0].label}`);
        }
        if (videoTracks.length > 0) {
            console.log(`Using video device: ${videoTracks[0].label}`);
        }

        peerRemote.ontrack = onRemoteStream;
        peerLocal.onicecandidate = iceCallbackLocal;
        peerRemote.onicecandidate = iceCallbackRemote;
        console.log('call: Created local and remote peer connection objects');

        console.log('call: Adding local stream tracks to local connection');
        window.localStream.getTracks().forEach(track => peerLocal.addTrack(track, window.localStream));

        createOffer()
    }

    function createOffer() {
        console.log(`createOffer:\n${offerOptions.toString()}`);
        peerLocal.createOffer(sendOffer, onError, offerOptions);
    }
    
    function sendOffer(desc) {
        console.log(`sendOffer:\n${desc.sdp}`);
        peerLocal.setLocalDescription(desc, () => {}, onError);
        send('OFFER', desc);
    }

    function onAnswer(desc) {
        console.log(`onAnswer:\n${desc.sdp}`);
        peerLocal.setRemoteDescription(desc, () => {}, onError);
    }

    // TODO other way around
    // RECEIVER - peerRemote
    
    function onOffer(desc) {
        console.log(`onOffer:\n${desc.sdp}`);
        peerRemote.setRemoteDescription(desc, createAnswer, onError);
    }

    function createAnswer() {
        console.log(`createAnswer:\n`);
        peerRemote.createAnswer(sendAnswer, onError, answerOptions);
    }

    function sendAnswer(desc) {
        console.log(`sendAnswer:\n${desc.sdp}`);
        send('ANSWER', desc);
        setLocal(desc);
    }

    function setLocal(desc) {
        console.log(`setLocal:\n${desc.sdp}`);
        peerRemote.setLocalDescription(desc, () => {}, onError);
    }
    
    // COMMON

    function hangup() {
        console.log('hangup: Closing local and remote peer connection objects');
        peerLocal.close();
        peerRemote.close();
        peerLocal = peerRemote = null;
        console.log('hangup: Closed local and remote peer connection objects');

        hangupButton.disabled = true;
        callButton.disabled = false;
    }

    // ICE - STUN, TURN

    function onCandidate(candidate) {
        console.log(`Adding ICE candidate: ${candidate ? candidate.candidate : '(null)'}`);
        // TODO fix candidate negotiation
        peerLocal.addIceCandidate(candidate, onAddIceCandidateSuccess, onAddIceCandidateError);
        peerRemote.addIceCandidate(candidate, onAddIceCandidateSuccess, onAddIceCandidateError);
    }

    function iceCallbackLocal(event) {
        send('CANDIDATE', event.candidate);
    }

    function iceCallbackRemote(event) {
        send('CANDIDATE', event.candidate);
    }

    function onAddIceCandidateSuccess(candidate) {
        console.log(`Added ICE candidate: ${candidate.toString()}`);
    }

    function onAddIceCandidateError(error) {
        console.log(`Failed to add ICE candidate: ${error.toString()}`, error);
    }

    return {
        onOffer: onOffer,
        onAnswer: onAnswer,
        onCandidate: onCandidate,
    };
}
