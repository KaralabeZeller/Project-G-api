function initCall(_send) {
    'use strict';

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

    var lobbyName = document.getElementById('lobbyName').value;

    const video1 = document.querySelector('video#video1');
    const video2 = document.querySelector('video#video2');

    let pc1Local;
    let pc1Remote;

    const offerOptions = {
        offerToReceiveAudio: 1,
        offerToReceiveVideo: 0,
    };

    function gotStream(stream) {
        console.log('Received local stream');
        video1.srcObject = stream;
        window.localStream = stream;
        callButton.disabled = false;
    }

    function start() {
        startButton.disabled = true;

        console.log('Requesting local stream');
        navigator.mediaDevices
            .getUserMedia({
                audio: true,
                video: false
            })
            .then(gotStream)
            .catch(e => console.log('getUserMedia() error: ', e));

        pc1Local = new RTCPeerConnection(servers);
        pc1Remote = new RTCPeerConnection(servers);
    }

    function call() {
        callButton.disabled = true;
        hangupButton.disabled = false;

        console.log('Starting calls');
        const audioTracks = window.localStream.getAudioTracks();
        const videoTracks = window.localStream.getVideoTracks();
        if (audioTracks.length > 0) {
            console.log(`Using audio device: ${audioTracks[0].label}`);
        }
        if (videoTracks.length > 0) {
            console.log(`Using video device: ${videoTracks[0].label}`);
        }

        pc1Remote.ontrack = gotRemoteStream1;
        pc1Local.onicecandidate = iceCallback1Local;
        pc1Remote.onicecandidate = iceCallback1Remote;
        console.log('pc1: created local and remote peer connection objects');

        window.localStream.getTracks()
            .forEach(track => pc1Local.addTrack(track, window.localStream));
        console.log('Adding local stream to pc1Local');

        pc1Local.createOffer(offerOptions)
            .then(sendOffer, onCreateSessionDescriptionError);

    }

    function onCreateSessionDescriptionError(error) {
        console.log(`Failed to create session description: ${error.toString()}`);
    }

    // TODO other way around
    
    //CALLER
    function sendOffer(desc) {
        console.log(`sendOffer\n${desc.sdp}`);
        pc1Local.setLocalDescription(desc, () => {}, onCreateSessionDescriptionError);
        send(desc, 'OFFER');

    }

    function gotAnswer(desc) {
        console.log(`gotAnswer()\n${desc.sdp}`);
        pc1Local.setRemoteDescription(desc, () => {}, onCreateSessionDescriptionError);
    }

    //RECEIVER
    function gotOffer(desc) {
        console.log(`gotOffer()\n${desc.sdp}`);
        pc1Remote.setRemoteDescription(desc, createAnswer, onCreateSessionDescriptionError);
    }

    function createAnswer() {
        console.log(`createAnswer()`);
        pc1Remote.createAnswer(sendAnswer, onCreateSessionDescriptionError);
    }

    function sendAnswer(desc) {
        console.log(`sendAnswer()\n${desc.sdp}`);
        send(desc, 'ANSWER');
        setLocal(desc);
    }

    function setLocal(desc) {
        console.log(`setLocal()`);
        pc1Remote.setLocalDescription(desc, () => {}, onCreateSessionDescriptionError);
    }

    function hangup() {
        console.log('Ending calls');
        pc1Local.close();
        pc1Remote.close();
        pc1Local = pc1Remote = null;
        hangupButton.disabled = true;
        callButton.disabled = false;
    }

    function gotRemoteStream1(e) {
        if (video2.srcObject !== e.streams[0]) {
            video2.srcObject = e.streams[0];
            console.log('pc1: received remote stream');
        }
    }

    function iceCallback1Local(event) {
        handleCandidate(event.candidate, pc1Remote, 'pc1: ', 'local');
    }

    function iceCallback1Remote(event) {
        handleCandidate(event.candidate, pc1Local, 'pc1: ', 'remote');
    }

    // TODO fix handleCandidate
    function handleCandidate(candidate, dest, prefix, type) {
        dest.addIceCandidate(candidate)
            .then(onAddIceCandidateSuccess, onAddIceCandidateError);
        console.log(`${prefix}New ${type} ICE candidate: ${candidate ? candidate.candidate : '(null)'}`);
    }

    function onAddIceCandidateSuccess() {
        console.log('AddIceCandidate success.');
    }

    function onAddIceCandidateError(error) {
        console.log(`Failed to add ICE candidate: ${error.toString()}`);
    }

    return {
        gotOffer: gotOffer,
        gotAnswer: gotAnswer,
    };
}
