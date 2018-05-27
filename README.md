# BLECar

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7061e2842fba4157bb070a54fac7eebb)](https://app.codacy.com/app/Metonimie/BLECar?utm_source=github.com&utm_medium=referral&utm_content=metonimie/BLECar&utm_campaign=badger)

## User Requirements

* The system must provide its users an interface to control a Radio-Controlled Car via WiFi/Bluetooth through an Android application.
* The system shall offer speed and steering control in order to move the car.
* The system will provide a live video feed to its users when controlling the car through the Android application.
* The system will provide the means to retrieve its current location (coordinates).
* The system may offer safety features such as automatic braking, speed limiting and collision avoidance.
* The system may offer computer vision features, such as object recognition and automatic lane detection.
* The system must be modular, providing ease of adding a new component (e.g. Accelerometer module).

## System Overview 

The overview of the system is depicted in **Figure 1**.
![Figure 1](Docs/Figure1.png?raw=true "Figure 1")
<p align="center">
  <i><b>Figure 1 - System Overview Diagram</b></i>
</p>    

**Car Controller subsystem** is responsible with controlling and collecting the data from all sensor modules through means such as *remote method calling*, making use of the **Event Bus Architecture** implemented by **DBus**. This module also serves as an interface to the services provided by the system for the Remote Control subsystem.    
**Speed Controller extension** provides a set of services in order to control the DC motor resposible with the forward-backward motion of the car.    
**Steering Controller extension** provides a set of services in order to control the DC motor responsible with steering the wheels of the car.    
**Ultrasonic Sensor extension** contains the low-level driver responsible with the *HC-SR04* sensor. The module provides cyclic readings of the distance to any obstacle in front of the vehicle, accesible through the service registered on DBus.    
**Camera extension** is the service responsible with capturing the stream from the RPi Camera v2 and forwarding it to the network, where it can be accesible to the *Mobile controller & consumer*.    
**GPS extension** represents the driver of the *VN2828U7G5LF* GPS sensor. The module provides a service through which the current latitude, longitude and altitude can be retrieved.    
**Remote Control subsystem** is the interface through which the embedded system is controlled. Its implementation consists of a Django Webserver through which internet connection and routing is performed.    
**Mobile controller & consumer** represents the *Android* application used to control the vehicle, view various metrics from the sensors (e.g. speed, distance), and display the video stream transmitted by the embedded subsystem.    
**OTA Configuration & Upgrade subsystem** is the module making the over-the-air configurations and upgrades to the sensors/the car controller subsystem possible. The subsystem provides an interface for an external user to send upgrade packets to the embedded system in a secure way - user must be authenticated and the system must be connected to a Wi-Fi Network.    
**Web interface** contains the *mainenance console* togheder with the update & configure panel used by an external administrator to modify various modules of the embedded system remotely.    

## Hardware Design

The hardware circuit design is depicted in **Figure 2**.
![Figure 2](Docs/Figure2.PNG?raw=true "Figure 2")
<p align="center">
  <i><b>Figure 2 - Hardware Diagram</b></i>
</p>

The real assembly of the system is shown in **Figure 3**.
![Figure 3](Docs/Figure3.png?raw=true "Figure 3")
<p align="center">
  <i><b>Figure 3 - Real Assembly</b></i>
</p>
 
## WebServer
The WebServer architecture is depicted in **Figure 4**.
![Figure 4](Docs/Figure4.png?raw=true "Figure 4")
<p align="center">
  <i><b>Figure 4 - WebServer Architecture</b></i>
</p>

The web server acts like a completely isolated module by itself. I donsen't have any dependecies on the sensor and accuator services. The communication of the web server and other components is handled via D-BUS.

Gunicorn and Nginx's lifecycle is controlled by systemd. 
To start them the following commands are used:

```bash
systemctl start gunicorn
systemctl start nginx
``` 
The systemd configuration file for Gunicorn is found in the `scripts` directory, under the name `gunicorn.service`

The Nginx website configuration can be found in `scripts/ccserversite`.

In case of redeployment please refer to this guide: [Nginx & Gunicorn Deployment Guide](https://www.digitalocean.com/community/tutorials/how-to-set-up-django-with-postgres-nginx-and-gunicorn-on-ubuntu-14-04)

### Web Server Internals

Gunicorn works by having master process that manages all the workers, the master doesn't know anything about the incomming requests and all the responses are handled by the workers.

A web worker could be of multiple types like async, sync, tornado and asyncio. In this case, the web server uses synchronous workers, which can handle only one request at a time. In order to serve thousands of requests per seccond Gunicorn needs only 4-12 workers.

### D-BUS: Short Introduction

D-BUS is a form of inter-process communicationa and it's based on the socket mechanism that is found in UNIX-like systems.

There are two busses the system bus and the session bus. The system bus is dedicated to system services and the session bus provides desktop services to user applications.

A D-BUS service contains the object, which implements an interface. 

* The service is a collection of objects which provide a specific set of features, it is identified by a name that is similar to the Java package naming convention, `pi.sensor`.
* The object can be dynamically created or removed and it is identified by an unique object path like `/pi/sensor/ultrasonic`.
* The interface extends the service name to something like `pi.sensor.Ultrasonic` and contains properties, methods and signals.

The security of D-BUS is handled by policy files.

### Over the Air Update

Over the air updates are supported and handled by the web interface at the endpoint /administration/ota.
In order to access the endpoint the user must first login at /admin, after that the user will be indentified by a 
session cookie that is set automatically after the login.

The user can then proceed to /administration/ota and update the desired services. In order to tighten security, only
a few whitelisted services can be updated, otherwise every file on the system could be updated with OTA, other services
can be added on request.

