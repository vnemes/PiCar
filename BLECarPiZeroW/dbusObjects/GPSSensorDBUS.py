#!/usr/bin/python3

import _thread
from drivers.GPSSensor import GPSSensor

from gi.repository import GLib
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

class GPSSensorDBUS(dbus.service.Object):

    gpsSensor = GPSSensor()

    def __init__(self):
        DBusGMainLoop(set_as_default=True)
        self.loop = GLib.MainLoop()
        bus_name = dbus.service.BusName('picar.sensor.gps', bus=dbus.SessionBus())
        dbus.service.Object.__init__(self, bus_name, '/picar/sensor/gps')
        self._startSensor()
        self.loop.run()

    def _startSensor(self):
        """notifies the driver to start the GPS sensor'"""
        _thread.start_new_thread(self.gpsSensor.start_measurement,())
        # while True:
        #     print(str(self.gpsSensor.get_gps()))
        #     time.sleep(2)


    @dbus.service.method('picar.sensor.gps')
    def getCoords(self):
        """returns the GPS object from the sensor"""
        gps = self.gpsSensor.get_gps()
        if gps.latitude:
            retVal={'latitude':gps.latitude, 'longitude':gps.longitude, 'altitude':gps.altitude,'real':1.0}
        else:
            retVal={'latitude':45.747255, 'longitude':21.226206, 'altitude':251.0, 'real':0.0}
        return dbus.Dictionary(retVal, signature=None)

    @dbus.service.method('picar.sensor.gps')
    def quit(self):
        """stops the meassurement, removes this object from the DBUS connection and exits"""
        self.gpsSensor.stop()
        self.loop.quit()
        return

if __name__ == "__main__":

    try:
        myservice = GPSSensorDBUS()
    except KeyboardInterrupt:
        print("keyboard interrupt received")
        myservice.quit()
    except Exception as e:
        print("Unexpected exception occurred: '{}'".format(str(e)))