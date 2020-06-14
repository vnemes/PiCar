# PiCar 
### A Distributed Embedded System for Electric Vehicles Remote Control using Driver Assistance Systems

[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/Naereen/StrapDown.js/blob/master/LICENSE)
[![GitHub contributors](https://img.shields.io/github/contributors/Naereen/StrapDown.js.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/contributors/)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://lbesson.mit-license.org/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/Naereen/StrapDown.js.svg)](https://GitHub.com/Naereen/StrapDown.js/issues?q=is%3Aissue+is%3Aclosed)

![Overview](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/acc1.jpeg)

- Electric vehicle embedded platform
- Remote control (Wi-Fi or BLE)
- Distributed embedded software application
- Android (REST) Client
- Driver Assistance Systems – Lane Keep Assist / Adaptive Cruise Control

<p float="left">
  <img src="https://github.com/vnemes/PiCar/blob/master/Docs/acc_cropped.gif" width="337" />
  <img src="https://github.com/vnemes/PiCar/blob/master/Docs/lka_short.gif" width="561" /> 
</p>

Technologies used:
- C / C++ for Bluetooth Low Energy embedded platform
- Python (Flask + RPyC) for Distributed backend platform
  - drivers for speed, steering, ultrasonic sensor, gps also in Python
- Java / Kotlin for Android client 
- Linux (systemd) services 

## Backend: Embedded Platform

- Pi Zero W – Tightly coupled, singletons
- Pi 3B+ – Microservice architecture (RPyC)
- Flask WebServer gateway – 4 Gunicorn workers
- Inter-service communication based on UDP RPC
- Service Registration and Discovery
- Steering & Acceleration controllers
  - H bridge / servo software pwm
- Ultrasonic sensor driver & filter 
  - median filter of size 5
- GPS sensor driver
  - NMEA serial string parsing
- Jevois smart vision driver

![Backend Architecture](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/Architecture-Backend.png)


## FrontEnd: Android Application 

- Connected session manager
  - Configurable persistent connection data classes
- Handling and acceleration control
  - 2 axis single joystick / 2 1 axis joysticks
- Distance monitoring 
  - Cyclic ultrasonic sensor endpoint readings
- Live Video Feed / Map Overlay
  - H264 stream or MapFragment
- Persistent preferences 


The Android Application acts as the main client of the system, providing
control over connection sessions, vehicle handling and acceleration, distance
monitoring, map or video feed overlay and connection configurations management.
It is compatible with both the low-power and the high performance
platform, and it can also control the electric platform using Bluetooth Low
Energy.

The application consists of 3 activities, namely ConnectActivity, Controller-
Activity and SettingsActivity that are written in Java and Kotlin.

### Main Menu (ConnectActivity)

This activity provides the management of connection configurations and
allows the user to connect to the embedded platform through different connection
types described later in this sub-section.

![ConnectActivity](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/connectactivity.jpg)

When the user decides to connect to the selected option, ControllerActivity
is started and the selected configuration data is bundled with the activity start
Intent in order to establish a connection using the IP, SSID or BLE UUID of the
target device. The following figure describes the simplified flow for connecting
to the target plaform using REST calls:

![ConnectActivity](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/sequence-android-enable.png)

### Remote Controller (ControllerActivity)

This activity is launched in an attempt to establish a connection to the plat-form.  After the connection has succeeded, the application provides the userone or two joysticks (based on preferences) to control the speed and steering of the vehicle.


![ControlActivity](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/controlactivity.jpg)

This activity also provides a video overlay with the live video feed from theplatform’s on-board camera or a map with the global position of the vehicle, acquired using its GPS sensor. This activity ensures that the connection tothe vehicle is stopped while the app is put in background and re-connectedwhen the app is resumed in order to save the embedded platform’s batteryand prevent useless data traffic.

### Settings (SettingsActivity)

The SettingsActivity can be accessed from any screen of the application,and it allows the personalization of various UI preferences, embedded platformparameters or enabling/disabling the driver assistance modules.The list of preferences is defined in thepreferences.xmlresource file, andthe change of their value can be detected after leaving the SettingsActivity byquerying the shared preferences manager for the respective key value pairs.

![SettingsActivity](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/settingsactivity.jpg)

### Android Wear Module

The Android application provides a wear OS module that displays the con-nection status to the embedded platform and allows the control of its speedand steering using a joystick.

![WearModule](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/wear.jpg)

The communication between the wear module and the application is per-formed through a messaging protocol. Its principle of operation includes registering the appropriate message receivers and broadcasters to a previouslydefined channel that abstracts the bluetooth connection between the deviceand the wearable.

## Driver Assistance Systems

### Adaptive Cruise Control

The Adaptive Cruise Control module ensures that the electric vehicle maintains a constant distance to the vehicle (obstacle) in front by comparing thevalues retrieved from the ultrasonic sensor to the target distance and applyingacceleration or braking pre-emptively.

The module retrieves the filtered distance values from the Ultrasonic SensorService with a frequency of 33 Hertz, the same frequency the sensor uses for data acquisition. The values are then fed into a PID Controller which has as output the target acceleration values to be sent to the Speed Driver of the Platform.

![AdaptiveCruiseControl](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/accflow.png)

The  target  distance  to  the  next  vehicle  is  hard-coded  to  50  centimetersbecause it is directly related to the PID controller’s parameters which havebeen computed using an empirical method:
- A __Proportional__ gain of -0.1 was identified to result in acceleration out-put values that keep the vehicle in a +/- 5 centimeters range of the target distance, proving stable enough to only cause minor oscillations atsteady-state.
- Setting the __Derivative__ gain of -0.015 ensured that the vehicle reacts pro-portionally to both small and big changes in distance, thus reducing the oscillations and improving its dynamic response.
- The __Integrative__ gain was set to 0 because the resulting +/- 5 cm rangewas satisfactory enough for the application, and even small values wouldcause the acceleration values to drift outside the steady state.The  values  outputted  by  the  PID  are  in  the  closed  interval  [-100,  +100]and represent the estimated acceleration percentages to be applied in order tobring the vehicle closer to the target distance.  The Python implementation ofthe PID controller is achieved through the use of Martin Lundberg’s simple_pidlibrary which is MIT licensed.

### Lane Keep Assist

- Jevois Smart Vision camera
- Road boundary estimation
- Vanishing point computation
- Algorithm output triggers vehicle steering

![AdaptiveCruiseControl](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/lka6.png)

The Lane Keep Assist algorithm uses visual cues from the camera in orderto estimate thevanishing pointof the road, which is sent to the steering driverto command the general direction of the vehicle in order to keep it centered inits lane.  The algorithm relies on the road surface having clear markings forboth sides of the lane, preferably using a shade that contrasts the base colorof the road.

## Hardware Overview

![Hardware Overview](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/hw-design.png)

- Raspberry Pi 
  - RPi 3B+ / RPi Zero W
-Brushed DC Motor - acceleration
  - H Bridge (20kHz PWM)
- Servomotor – steering
  - 50 Hz, 1-2 ms pulses
- Ultrasonic Sensor – distance measurement
  - Sample time 33 Hz
- GPS Sensor

## Deployment and Development Process

In order to remotely develop and deploy software to the Raspberry Pi, I first
needed to establish a secure link between my development machine and the
Pi. This was achievable by enabling and securing ssh on the target platform
by changing the port from 22 to 443, disabling password authentication
and enabling public private key authentication based on certificates.

![Deployment Process](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/deployment.jpg)

Apart from having the ability to remotely configure the Raspberry Pi even
from other networks (after opening the port on my router at home and setting
up dynamic DNS from my network provider), enabling ssh has also allowed the
use of JetBrains’ IntelliJ IDEA IDE for deploying (using SFTP), running and
debugging the software directly on the target platform’s Python interpreter.



The following section is an excerpt from the diploma thesis document:
## Abstract 

During recent years, electrification, connectivity and Driver Assistance Systems
have become the topics of most interest in the automotive domain with
the goal of reducing carbon emissions and increasing passenger safety. While
the task of autonomous driving remains an open challenge due to its realworld
implications and real-time constraints, attempts have been made towards
implementing systems and algorithms that currently achieve a confident
level of autonomy in a limited set of circumstances.    

This thesis aims at presenting a proof-of-concept remote-controlled electric
vehicle that provides a number of basic autonomous driving features. The
system to be described allows an Android consumer to control the speed and
direction of the vehicle, display a live video feed from the on-board camera,
view the exact location of the vehicle via its GPS sensor and on-demand driver
assistance features such as Collision Avoidance, Adaptive Cruise Control and
Lane Keep Assist.    

The hardware platform of choice is the Raspberry Pi which, along with its
Linux distribution - Raspbian, allowed for fine-grained customization capabilities
regarding the orchestration of services, hardware interfaces and available
libraries. Coupled with the use of Python as the programming language of
choice for the embedded application, these design considerations allowed for
fast development cycles during prototyping and testing.
