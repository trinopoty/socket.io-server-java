var io = require('socket.io-client');
var port = process.env.PORT || 3000;

var binaryData = new ArrayBuffer(8);
var view = new Uint8Array(binaryData);
view[0] = 0;
view[1] = 1;
view[2] = 2;
view[3] = 3;
view[4] = 4;
view[5] = 5;
view[6] = 6;
view[7] = 7;

var socket = io('http://127.0.0.1:' + port, {
    autoConnect: false,
    transports: ['websocket']
});
socket.on('foo', function (data) {
    if (data && data.length === 8) {
        process.exit(0);
    }
});
socket.connect();

setTimeout(function () {
    process.exit(1);
}, 2000);