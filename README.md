# dockerbasic
Docker example for client-server application.
This is a java application which runs and accepts connection on port specified in the environment variable - HOST_PORT 


# Building the docker images

~~~~~
 cd dockerbasics
 docker build -t="server" -f Dockerfile_server .
 docker build -t="client" -f Dockerfile_client .

~~~~~~~


# Verify if images are created

~~~~~~~~
 $  docker images

 #output 

	REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
	client              latest              0a5b90fbc5f7        27 hours ago        585.9 MB
	server              latest              8b6bd1c4c26d        27 hours ago        585.9 MB

~~~~~~~~~~


# Spinning up docker containers

Server

~~~~~~~~~
	docker run -e HOST_PORT=25000 -p 25000:25000 server
~~~~~~~~~


Client

~~~~~~~
	docker run -e HOST_IP=<ip-address where server is running> -e HOST_PORT=25000 client

~~~~~~~


#Output

Client

Client was started by the command - "docker run -e HOST_IP=10.20.14.32 -e HOST_PORT=25000 client" 
~~~~~~~

posting messages on 10.20.14.32:25000
Message sent to the server : Hello from XOR_0
Message sent to the server : Hello from XOR_1
Message sent to the server : Hello from XOR_2
Message sent to the server : Hello from XOR_3

~~~~~~~

Server

Server was started by the command - "docker run -e HOST_PORT=25000 -p 25000:25000 server"
~~~~~~~

Server Started and listening to the port : 25000
Message received : Hello from XOR_0
Message received : Hello from XOR_1
Message received : Hello from XOR_2
Message received : Hello from XOR_3

~~~~~~~