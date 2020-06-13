# PiCar - Distributed Embedded System for Electric Vehicles Remote Control using Driver Assistance

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7061e2842fba4157bb070a54fac7eebb)](https://app.codacy.com/app/Metonimie/BLECar?utm_source=github.com&utm_medium=referral&utm_content=metonimie/BLECar&utm_campaign=badger)

## System Overview

## PiCar's Architecture

At a high-level, the system can be viewed as a client-server architecture,
with the embedded platform playing the role of a server handling requests
from its Android/Web clients.

## Hardware Overview

This section presents an overview of the hardware components with their
electrical specification, the electrical diagram depicting the ports and the connections
between the modules and the interaction of the system and its peripherals.

![Hardware Overview](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/hw-design.png)

The main hardware component of the embedded platform is the __Raspberry
Pi__. Multiple sensors are connected to its GPIO and serial ports in order to acquire and pre-process environmental information with regards to the position
and distance of the vehicle. The Raspberry Pi is also responsible with driving
the servo-motor used for controlling the steering of the electric vehicle and
the H-Bridge that interfaces with the high-amperage brushed DC motor that
provides forward-backward motion.

The movement of the electric vehicle is accomplished by the brushed DC
motor that drives all 4 wheels through the driveshaft and the front and rear
differential. The motor is powered by a continuous voltage source of 7 volts,
driven by the Pololu G2 High-Power Motor Driver. The motor driver supports
PWM frequencies as high as 100 kHz, but for this application 20 kHz was chosen
since it is high enough to be ultrasonic, which results in quieter operation.
The detailed python programming of the H-Bridge driver is further described
in the next section.

The servomotor used for controlling the steering of the PiCar electric vehicle
uses position-only sensing via a potentiometer and bang-bang control of
the motor: it always rotates at full speed (or is stopped). This type of servomotor
is not widely used in industrial motion control, but it is the most common
of the simple and cheap servos used for radio-controlled models. There are
three connections available to control this servomotor: Vcc, Gnd and Control.
After a voltage potential of 5 volts is supplied between the Vcc and ground
terminals, a 50Hz pulse-signal (out of which the on-time can vary from 1ms
to 2ms) is used to control the position of the servo. In order to protect the
Raspberry Pi’s internal constant voltage source that can barely handle the 1
Ampere full load current draw of the servomotor, its VCC is connected to the H
Bridge’s regulated power supply (after voltage protection), accessible through
the VM pin.

The ultrasonic sensor is driven by the Raspberry Pi using its TRIG (trigger)
pin. When a specific duration pulse is applied to this pin, the Ultrasonic
sensor will send out the chirp signal through its emitter and will then start
listening for the reflected signal with its receiver. When the previously sent
pattern has been successfully received by the sensor, a fixed duration pulse is
sent on the ECHO pin. The mathematical background of finding the distance
based on the round trip time of the signal can be found in the previous chapter.
Since the ultrasonic distance sensor operates at a voltage of 5 Volts, the
Raspberry Pi’s input pins cannot directly process pulses sent by the sensor
without risking damaging the port. In order to avoid this, a voltage divider
having resistor values R1 = 1000 Ohm, R2= 2000 Ohm is connected between the RPi’s
pin 12, the sensor’s ECHO pin and Ground. The GPS sensor’s two pins, Rx
and Tx are connected to the Raspberry Pi’s Transmit and Receive pins, in
order to achieve a bi-directional serial port between the two devices. After
the sensor finished its initialization routine and has established line of sight
with at least 3 satellites, it will start outputting NMEA formatted commands
representing time and position information on the serial channel.

## Software Implementation

### Deployment and Development Process

The development process for embedded systems is usually significantly
slower compared to classic applications that run on a user’s personal computer
or in the cloud, and testing the system often takes as much time as
the development itself. In order to improve the development times on the embedded
platform, I have opted for a design and implementation methodology
focused on performing incremental changes to components and verifying the
expected outcomes directly on the device under test.

In order to remotely develop and deploy software to the Raspberry Pi, I first
needed to establish a secure link between my development machine and the
Pi. This was achievable by enabling and securing ssh (Secure Shell) on the machine
by changing the port from 22 to 443, disabling password authentication
and enabling public private key authentication based on certificates.

![Deployment Process](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/deployment.jpg)

Apart from having the ability to remotely configure the Raspberry Pi even
from other networks (after opening the port on my router at home and setting
up dynamic DNS from my network provider), enabling ssh has also allowed the
use of JetBrains’ IntelliJ IDEA IDE for deploying (using SFTP), running and
debugging the software directly on the target platform’s Python interpreter.

### Backend: Embedded Platform

![Backend Architecture](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/Architecture-Backend.png)

The architecture depicted above contains both the low-power, tightly
coupled system and the distributed, micro-service based platform. The uninterrupted
lines represent direct dependencies between the modules, while
the dotted lines represent service registrations, remote calls, and callbacks.

### FrontEnd: Android Application

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

The configurations are persistent, i.e. the values that were previously saved
will be available between an application restarts

The user interface is split into two main areas of interest: on the left a ListView
containing the available configurations, and on the right a Fragment that gets
displayed with information corresponding to the selected configuration (name,
IP, connection type image, etc.) The following methods of communication with
the platform are available, as described in the ConectionTypeEn enum:
- the Pi’s local IP when both the client and the platform are on the same
network
- the Pi’s global IP if the network the platform resides in has the appropriate
ports forwarded
- the Pi’s WiFi Network SSID and its IP inside its own network if the platform
created its own wireless HotSpot
- the Bluetooth Low Energy Device UUID if the platform is capable of this
kind of communication

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

The Adaptive Cruise Control module ensures that the electric vehicle main-tains a constant distance to the vehicle (obstacle) in front by comparing thevalues retrieved from the ultrasonic sensor to the target distance and applyingacceleration or braking pre-emptively.

The module retrieves the filtered distance values from the Ultrasonic SensorService with a frequency of 33 Hertz, the same frequency the sensor uses for data acquisition. The values are then fed into a PID Controller which has as output the target acceleration values to be sent to the Speed Driver of the Platform.

![AdaptiveCruiseControl](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/accflow.png)

The  target  distance  to  the  next  vehicle  is  hard-coded  to  50  centimetersbecause it is directly related to the PID controller’s parameters which havebeen computed using an empirical method:
- A __Proportional__ gain of -0.1 was identified to result in acceleration out-put values that keep the vehicle in a +/- 5 centimeters range of the target distance, proving stable enough to only cause minor oscillations atsteady-state.
- Setting the __Derivative__ gain of -0.015 ensured that the vehicle reacts pro-portionally to both small and big changes in distance, thus reducing the oscillations and improving its dynamic response.
- The __Integrative__ gain was set to 0 because the resulting +/- 5 cm rangewas satisfactory enough for the application, and even small values wouldcause the acceleration values to drift outside the steady state.The  values  outputted  by  the  PID  are  in  the  closed  interval  [-100,  +100]and represent the estimated acceleration percentages to be applied in order tobring the vehicle closer to the target distance.  The Python implementation ofthe PID controller is achieved through the use of Martin Lundberg’s simple_pidlibrary which is MIT licensed.

### Lane Keep Assist

The Lane Keep Assist algorithm uses visual cues from the camera in orderto estimate thevanishing pointof the road, which is sent to the steering driverto command the general direction of the vehicle in order to keep it centered inits lane.  The algorithm relies on the road surface having clear markings forboth sides of the lane, preferably using a shade that contrasts the base colorof the road.


![AdaptiveCruiseControl](https://github.com/vnemes/PiCar/blob/Diploma-Thesis/Docs/lka6.png)










The following sections are excerpts from the thesis document:
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

## Main Accomplishments

Besides the distributed and open nature of its architecture, the main achievements
of the PiCar platform are as follows:
- In the case of the Pi Zero variant - efficient remote control of an electric
vehicle with low-power constraints, live video feed and basic environmental
sensory information consumed by the android application
- In the case of the Pi 3B+ equipped system - accomplishment of basic
(L2) autonomous driving tasks and low latency of information exchanged
between the remote and the platform

The existence of two electric vehicle platforms aims to prove the robustness
of the application: one is a low-power, low-complexity modified "toy" car running
basic control, vision and location services, while the other is a high performance,
semi-professional RC vehicle capable of computationally-intensive
autonomous driving features - both running the same software. This was only
achievable through the distributed nature of the application and the design
and architectural patterns applied throughout the development of the system.

### References:
* https://pinout.xyz/
* https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
* http://docs.gunicorn.org/en/stable/deploy.html
* https://developer.android.com/training/volley/
* https://code.tutsplus.com/tutorials/streaming-video-in-android-apps--cms-19888
* https://tutorials-raspberrypi.com/measuring-rotation-and-acceleration-raspberry-pi/
* https://pypi.org/project/smbus-cffi/0.5.1/
* https://www.linuxjournal.com/article/10455
* https://www.raspberrypi.org/documentation/configuration/wireless/access-point.md#internet-sharing
* https://github.com/ShawnBaker/RPiCameraViewer
* https://stackoverflow.com/questions/6146131/python-gps-module-reading-latest-gps-data
* https://tutorials-raspberrypi.com/measuring-rotation-and-acceleration-raspberry-pi/

