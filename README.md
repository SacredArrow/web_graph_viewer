# web_graph_viewer
## Overview
This client-server application allows you to build and watch graphs from data presented in format "x_coord y_coord"
## Installation
#### Server
To start server run 
```
java -jar server.jar
```

To rebuild project run
```
mvn compile assembly:single
```

Websocket will be available on ws://localhost:8000/socket
#### Front-end
Run
```
npm i
npm run dev
```
Application will be run on http://localhost:8080
