#!/usr/bin/python3

import gi

from drivers.PiCarController import PiCarController

gi.require_version('Gtk', '3.0')
from gi.repository import Gtk
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop


class PiCarControllerDBUS(dbus.service.Object):

    piCarController = PiCarController()

    def __init__(self):
        bus_name = dbus.service.BusName('picar.control.speedsteering', bus=dbus.SessionBus())
        dbus.service.Object.__init__(self, bus_name, '/picar/control/speedsteering')

    @dbus.service.method('picar.control.speedsteering')
    def setSpeed(self, direction, speed):
        """sets the speed of the DC motor forward or backward as specified by direction"""
        self.piCarController.set_speed(direction, speed)

    @dbus.service.method('picar.control.speedsteering')
    def setSteering(self, direction, steering):
        """sets the speed of the DC motor forward or backward as specified by direction"""
        self.piCarController.set_steering(direction, steering)

    @dbus.service.method('picar.control.speedsteering')
    def quit(self):
        """stops the meassurement, removes this object from the DBUS connection and exits"""
        self.piCarController.stop()
        self.remove_from_connection()
        Gtk.main_quit()
        return


if __name__ == "__main__":
    DBusGMainLoop(set_as_default=True)
    myservice = PiCarControllerDBUS()
    Gtk.main()
