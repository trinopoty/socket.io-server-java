var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var socket = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket.on('foo', function (bar, callback) {
    if (bar === 'bar') {
        callback('baz');
        process.exit(0);
    }
});
socket.connect();

setTimeout(function () {
    process.exit(1);
}, 2000);