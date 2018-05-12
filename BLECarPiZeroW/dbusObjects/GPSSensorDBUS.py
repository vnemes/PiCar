#!/usr/bin/python3

import _thread
import time
import gi
from drivers.GPSSensor import GPSSensor

gi.require_version('Gtk', '3.0')
from gi.repository import Gtk
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

class GPSSensorDBUS(dbus.service.Object):

    gpsSensor = GPSSensor()

    def __init__(self):
        bus_name = dbus.service.BusName('picar.sensor.gps', bus=dbus.SessionBus())
        dbus.service.Object.__init__(self, bus_name, '/picar/sensor/gps')
        self._startSensor()

    def _startSensor(self):
        """notifies the driver to start the GPS sensor'"""
        _thread.start_new_thread(self.gpsSensor.start_measurement,())
        while True:
            print(str(self.gpsSensor.get_gps()))
            time.sleep(2)


    @dbus.service.method('picar.sensor.gps')
    def getCoords(self):
        """returns the GPS object from the sensor"""
        return self.gpsSensor.get_gps()

    @dbus.service.method('picar.sensor.gps')
    def quit(self):
        """stops the meassurement, removes this object from the DBUS connection and exits"""
        self.gpsSensor.stop()
        self.remove_from_connection()
        Gtk.main_quit()
        return

if __name__ == "__main__":
    DBusGMainLoop(set_as_default=True)
    myservice = GPSSensorDBUS()
    Gtk.main()