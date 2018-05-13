#!/usr/bin/python3

import _thread
from drivers.UltrasonicSensor import UltrasonicSensor

from gi.repository import GLib
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

class UltrasonicSensorDBUS(dbus.service.Object):

    ultrasonicSensor = UltrasonicSensor()

    def __init__(self):
        DBusGMainLoop(set_as_default=True)
        self.loop = GLib.MainLoop()
        bus_name = dbus.service.BusName('picar.sensor.ultrasonic', bus=dbus.SessionBus())
        dbus.service.Object.__init__(self, bus_name, '/picar/sensor/ultrasonic')
        self._startDistanceMeasurement()
        self.loop.run()

    def _startDistanceMeasurement(self):
        """notifies the driver to start the distance measurement'"""
        _thread.start_new_thread(self.ultrasonicSensor.start_measurement,())
        # while True:
        #     print(str(self.ultrasonicSensor.get_distance()) + ' cm')
        #     time.sleep(0.2)


    @dbus.service.method('picar.sensor.ultrasonic')
    def getDistance(self):
        """returns the distance returned by the sensor"""
        return self.ultrasonicSensor.get_distance()

    @dbus.service.method('picar.sensor.ultrasonic')
    def quit(self):
        """stops the meassurement, removes this object from the DBUS connection and exits"""
        self.ultrasonicSensor.stop()
        self.loop.quit()
        return

if __name__ == "__main__":

    try:
        myservice = UltrasonicSensorDBUS()
    except KeyboardInterrupt:
        print("keyboard interrupt received")
        myservice.quit()
    except Exception as e:
        print("Unexpected exception occurred: '{}'".format(str(e)))