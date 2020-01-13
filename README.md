# Socket.IO Java
[![Build Status](https://travis-ci.org/trinopoty/socket.io-server-java.png?branch=master)](https://travis-ci.org/trinopoty/socket.io-server-java) [![codecov](https://codecov.io/gh/trinopoty/socket.io-server-java/branch/master/graph/badge.svg)](https://codecov.io/gh/trinopoty/socket.io-server-java)

This is the Socket.IO Server Library for Java ported from the [JavaScript server](https://github.com/socketio/socket.io).

See also: [Socket.IO-client Java](https://github.com/socketio/socket.io-client-java)

## Features
This library supports all of the features the JS server does, including events, options and upgrading transport.

## Documentation
Complete documentation can be found [here](https://trinopoty.github.io/socket.io-server-java/).

## Installation
The latest artifact is available on Maven Central.

### Maven
Add the following dependency to your `pom.xml`.

```xml
<dependencies>
  <dependency>
    <groupId>io.socket</groupId>
    <artifactId>socket.io-server</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Gradle
Add it as a gradle dependency in `build.gradle`.

```groovy
compile ('io.socket:socket.io-server:1.0.0')
```

## License

MIT
