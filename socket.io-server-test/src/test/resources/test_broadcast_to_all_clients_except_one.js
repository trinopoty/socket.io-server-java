var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var barReceived = [false, false];

var socket1 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    forceNew: true,
    transports: ['websocket']
});
socket1.on('connect', function () {
    socket1.emit('foo');
});
socket1.on('bar', function () {
    barReceived[0] = true;
});

var socket2 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    forceNew: true,
    transports: ['websocket']
});
socket2.on('connect', function () {
    socket2.emit('foo');
});
socket2.on('bar', function () {
    barReceived[1] = true;
});

socket1.connect();
socket2.connect();

setTimeout(function () {
    console.log(JSON.stringify(barReceived));

    if (barReceived[0] !== barReceived[1]) {
        process.exit(0);
    } else {
        process.exit(1);
    }
}, 2000);