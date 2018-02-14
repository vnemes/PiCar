import dbus
import dbus.mainloop.glib

try:
    from gi.repository import GObject
except ImportError:
    import gobject as GObject


from BlueZComponents import *
from PiCarController import *

mainloop = None


class SteeringChrc(Characteristic):
    STEERING_UUID = 'beb5483e-36e1-4688-b7f5-ea07361b26a8'

    def __init__(self, bus, index, service):
        Characteristic.__init__(
            self, bus, index,
            self.STEERING_UUID,  # use the row number to build the UUID
            ['write'],
            service)

    def WriteValue(self, value, options):
        print('SteeringCharacteristic Write: ' + repr(value))
        car_setsteering(value[:1])
        #set_display_row(self.display, self.row)


class SpeedChrc(Characteristic):
    SPEED_UUID = '2eabb1e1-ae0f-4eb8-bfdc-f564ad55f359'

    def __init__(self, bus, index, service):
        Characteristic.__init__(
            self, bus, index,
            self.SPEED_UUID,
            ['write'],
            service)

    def WriteValue(self, value, options):
        print('SpeedCharacteristic Write: ' + repr(value))
        car_setspeed(value[:1])
        # set_display_row(self.display, self.row)


class MovementService(Service):
    MVMT_SVC_UUID = '4fafc201-1fb5-459e-8fcc-c5c9c331914b'

    def __init__(self, bus, index):
        Service.__init__(self, bus, index, self.MVMT_SVC_UUID, True)
        self.add_characteristic(SteeringChrc(bus, 0, self))
        self.add_characteristic(SpeedChrc(bus, 1, self))


class BLEApplication(Application):
    def __init__(self, bus):
        Application.__init__(self, bus)
        self.add_service(MovementService(bus, 0))


class BLEAdvertisement(Advertisement):
    def __init__(self, bus, index):
        Advertisement.__init__(self, bus, index, 'peripheral')
        self.add_service_uuid(MovementService.MVMT_SVC_UUID)
        self.include_tx_power = True


def register_ad_cb():
    """
    Callback if registering advertisement was successful
    """
    print('Advertisement registered')


def register_ad_error_cb(error):
    """
    Callback if registering advertisement failed
    """
    print('Failed to register advertisement: ' + str(error))
    mainloop.quit()


def register_app_cb():
    """
    Callback if registering GATT application was successful
    """
    print('GATT application registered')


def register_app_error_cb(error):
    """
    Callback if registering GATT application failed.
    """
    print('Failed to register application: ' + str(error))
    mainloop.quit()


def main():
    global mainloop

    dbus.mainloop.glib.DBusGMainLoop(set_as_default=True)

    bus = dbus.SystemBus()

    # Initialize GPIO
    car_init()

    # Get ServiceManager and AdvertisingManager
    service_manager = get_service_manager(bus)
    ad_manager = get_ad_manager(bus)

    # Create gatt services
    app = BLEApplication(bus)

    # Create advertisement
    test_advertisement = BLEAdvertisement(bus, 0)

    mainloop = GObject.MainLoop()

    # Register gatt services
    service_manager.RegisterApplication(app.get_path(), {},
                                        reply_handler=register_app_cb,
                                        error_handler=register_app_error_cb)

    # Register advertisement
    ad_manager.RegisterAdvertisement(test_advertisement.get_path(), {},
                                     reply_handler=register_ad_cb,
                                     error_handler=register_ad_error_cb)

    try:
        mainloop.run()
    except KeyboardInterrupt:
        car_cleanup()


if __name__ == '__main__':
    main()