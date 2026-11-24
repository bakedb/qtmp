# QTMP
Quick Time Messaging Protocol (QTMP) is a simple instant messaging stack built in Java.

## Setting up your server
Make sure you have Docker and Docker Compose installed.

Make a new directory for the server:
`mkdir qtmp && cd qtmp`

Pull the docker-compose.yml file:
`curl -O https://raw.githubusercontent.com/bakedb/qtmp/main/docker-compose.yml`

Pull the config file:
`curl -O https://raw.githubusercontent.com/bakedb/qtmp/main/server.properties`

Be sure to set all of the options in the config file before starting the server with 
`docker compose up -d`.

Congrats, you're done! All you need to do now is forward whatever TCP port is set up in the configuration file (7867 by default).

## Interacting with the Server
Run the `QTMPClient.java` file in the repository to interact with the server.