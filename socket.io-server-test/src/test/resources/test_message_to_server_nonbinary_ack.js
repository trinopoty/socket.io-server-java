var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var socket = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket.on('connect', function () {
    socket.emit('foo', 1, 'bar', function(baz) {
        // Ack received
        if (baz === 'baz') {
            process.exit(0);
        }
    });
});
socket.connect();

setTimeout(function () {
    process.exit(1);
}, 2000);