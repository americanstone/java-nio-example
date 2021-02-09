# java-nio-example


![socketchannel.jpg.webp](example/doc/socketchannel.jpg)

https://medium.com/coderscorner/tale-of-client-server-and-socket-a6ef54a74763

https://examples.javacodegeeks.com/core-java/nio/java-nio-socket-example/

```json5
Server thread: main Server started...
server interest OP_ACCEPT
server blocking/wait on client connect event
Client started! connect to server 8090...
Client started! connect to server 8090...
Server connected to client: /127.0.0.1:64557
Selector interest OP_READ
server blocking/wait on client connect event
Server connected to client: /127.0.0.1:64558
Selector interest OP_READ
server blocking/wait on client connect event
client-A: client msg test1
client-B: client msg test1
Got: client-A: client msg test1
server blocking/wait on client connect event
Got: client-B: client msg test1
server blocking/wait on client connect event
client-A:client msg test2
client-B:client msg test2
Got: client-A:client msg test2
server blocking/wait on client connect event
Got: client-B:client msg test2
server blocking/wait on client connect event
client-B: client msg test3
client-A: client msg test3
Got: client-B: client msg test3
Got: client-A: client msg test3
server blocking/wait on client connect event
Connection closed by client: /127.0.0.1:64557
server blocking/wait on client connect event
Connection closed by client: /127.0.0.1:64558
server blocking/wait on client connect event
```