var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var fooReceived = [false, false, false];

var socket1 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket1.on('connect', function() {
    socket1.emit('join');
});
socket1.on('foo', function () {
    fooReceived[0] = true;
});

var socket2 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket2.on('connect', function() {
    socket2.emit('join');
});
socket2.on('foo', function () {
    fooReceived[1] = true;
});

var socket3 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket3.on('foo', function () {
    fooReceived[2] = true;
});

socket1.connect();
socket2.connect();
socket3.connect();

setTimeout(function () {
    if (fooReceived[0] && fooReceived[1] && !fooReceived[2]) {
        process.exit(0);
    } else {
        process.exit(1);
    }
}, 2000);