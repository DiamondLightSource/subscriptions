# Subscriptions Service Prototype

## Overview
Supports subscription to public event topics (provided internally by either ActiveMQ or RabbitMQ) using STOMP over websockets.

Clients can connect on port ```9020``` to the ```/subscriptions``` endpoint and subscribe to topic names of the form

```properties
public.somename
public.somename.someothername
```
etc. All topic names specified by clients must start with the prefix ```public.``` otherwise they will be rejected.

When subscribing to a topic, clients must supply a ```receipt``` header in the STOMP SUBSCRIBE frame in addition to the 
normal mandatory ```id``` header. The value of the receipt field can be any value, provided that it is unique for each 
new subscription. For this reason it is recommended to use a format of the form:

```properties
idOfClient-idWithinClient
```

This value will be returned by the Service in a RECEIPT frame to indicate successful creation of the subscxription and 
can thus be used to keep track of a client's active subscriptions. No receipt will be provided if the subscription 
attempt is unsuccessful, so clients should set a timeout after which, if no receipt is received, they will interpret the 
subscription attempt to have failed.

All common config for the application is stored in ```src/main/resources/application.yml``` and loaded via the 
```BrokerConnection``` and ``WebSocket`` Records with broker specific setup in the appropriate adjacent subdirectory.

## Broker Initialisation
Either ActiveMQ or RabbitMQ can be run under ```podman``` using the Container files provided, which will establish a 
running instance of the selected broker with the standard setup expected by the subscription service. 

To initialise your broker of choice run the container file from the appropriate subdirectory of  ```resources```. 
N.B you may need to ```chmod +x``` the file so that it is executable on your file system.  So, for instance, to start 
ActiveMQ enter:

```shell
${YOUR_PROJECT_PATH}/src/main/resources/activemq/active
```

In either case, a broker image will be pulled from docker.io (assuming it is not already present in your image store) 
and run under ```podman``` with the appropriate settings. Once it has started succesfully, you can run the subscriptions
service and it will connect on the standard STOMP port 61613 as the user ```subscription```.

You cannot run both broker containers simultaneously as the ports will clash. If you want to switch to the other broker 
you will need to stop the service and then run ```podman stop <running container id>```, before then executing the 
alternative container file. After this the subscriptions service can be restarted and will connect to the new broker.