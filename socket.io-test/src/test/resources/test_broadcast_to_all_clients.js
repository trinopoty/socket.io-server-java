var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var fooReceived = [false, false];
function testReceived() {
    if (fooReceived[0] && fooReceived[1]) {
        process.exit(0);
    }
}

var socket1 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket1.on('foo', function () {
    fooReceived[0] = true;
    testReceived();
});

var socket2 = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket2.on('foo', function () {
    fooReceived[1] = true;
    testReceived();
});

socket1.connect();
socket2.connect();

setTimeout(function () {
    process.exit(1);
}, 2000);