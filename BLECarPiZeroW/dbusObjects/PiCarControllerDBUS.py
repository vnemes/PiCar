#!/usr/bin/python3

import gi

from drivers.PiCarController import PiCarController

gi.require_version('Gtk', '3.0')
from gi.repository import GLib
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

piCarController = PiCarController()


class PiCarControllerDBUS(dbus.service.Object):


    def __init__(self):
        DBusGMainLoop(set_as_default=True)
        self.loop = GLib.MainLoop()
        bus_name = dbus.service.BusName('picar.control.speedsteering', bus=dbus.SessionBus())
        dbus.service.Object.__init__(self, bus_name, '/picar/control/speedsteering')
        self.loop.run()

    @dbus.service.method('picar.control.speedsteering')
    def setSpeed(self, direction, speed):
        """sets the speed of the DC motor forward or backward as specified by direction"""
        piCarController.set_speed(direction, speed)

    @dbus.service.method('picar.control.speedsteering')
    def setSteering(self, direction, steering):
        """sets the speed of the DC motor forward or backward as specified by direction"""
        piCarController.set_steering(direction, steering)

    @dbus.service.method('picar.control.speedsteering')
    def quit(self):
        """stops the meassurement, removes this object from the DBUS connection and exits"""
        piCarController.stop()
        self.loop.quit()
        return


if __name__ == "__main__":

    try:
        myservice = PiCarControllerDBUS()
    except KeyboardInterrupt:
        print("keyboard interrupt received")
        myservice.quit()
    except Exception as e:
        print("Unexpected exception occurred: '{}'".format(str(e)))

