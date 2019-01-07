var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var fooReceived = [false, false];

var socket1 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket1.on('connect', function () {
    setTimeout(function () {
        socket1.emit('foo');
    }, 100);
});
socket1.on('foo', function () {
    fooReceived[0] = true;
});

var socket2 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket2.on('connect', function () {
    setTimeout(function () {
        socket2.emit('foo');
    }, 100);
});
socket2.on('foo', function () {
    fooReceived[1] = true;
});

socket1.connect();
socket2.connect();

setTimeout(function () {
    if (fooReceived[0] && !fooReceived[1]) {
        process.exit(0);
    } else {
        process.exit(1);
    }
}, 2000);