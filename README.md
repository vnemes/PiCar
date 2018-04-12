# BLECar

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7061e2842fba4157bb070a54fac7eebb)](https://app.codacy.com/app/Metonimie/BLECar?utm_source=github.com&utm_medium=referral&utm_content=metonimie/BLECar&utm_campaign=badger)

## User Requirements

* The system must provide its users an interface to control a Radio-Controlled Car via WiFi/Bluetooth through an Android application.
* The system shall offer speed and steering control in order to move the car.
* The system will provide a live video feed to its users when controlling the car through the Android application.
* The system may offer safety features such as automatic braking, speed limiting and collision avoidance.
* The system may offer computer vision features, such as object recognition and automatic lane detection.
* The system must be modular, providing ease of adding a new component (e.g. GPS module).

## System Overview 

The overview of the system is depicted in **Figure 1**.
![Figure 1](Docs/Figure1.png?raw=true "Figure 1")
**Car Controller subsystem** is responsible with controlling and collecting the data from all sensor modules through means such as *remote method calling*, making use of the **Event Bus Architecture** implemented by **DBus**. This module also serves as an interface to the services provided by the system for the Remote Control subsystem.    
**Speed Controller extension** provides a set of services in order to control the DC motor resposible with the forward-backward motion of the car.    
**Steering Controller extension** provides a set of services in order to control the DC motor responsible with steering the wheels of the car.    
**Ultrasonic Sensor extension** contains the low-level driver responsible with the *HC-SR04* sensor. The module provides cyclic readings of the distance to any obstacle in front of the vehicle, accesible through the service registered on DBus.    
**Camera extension** is the service responsible with capturing the stream from the RPi Camera v2 and forwarding it to the network, where it can be accesible to the *Mobile controller & consumer*.    
**Accelerometer extension** represents the driver of the *GY-521* accelerometer & gyroscope sensor. The module provides a set of services through which the current speed and direction is determined.    
**Remote Control subsystem** is the interface through which the embedded system is controlled. Its implementation consists of a Django Webserver through which internet connection and routing is performed.    
**Mobile controller & consumer** represents the *Android* application used to control the vehicle, view various metrics from the sensors (e.g. speed, distance), and display the video stream transmitted by the embedded subsystem.    
**OTA Configuration & Upgrade subsystem** is the module making the over-the-air configurations and upgrades to the sensors/the car controller subsystem possible. The subsystem provides an interface for an external user to send upgrade packets to the embedded system in a secure way - user must be authenticated, payload must be digitally signed, the system must be connected to a Wi-Fi Network.    
**Web interface** contains the *mainenance console* togheder with the update & configure panel used by an external administrator to modify various modules of the embedded system remotely.     
