from components.drivers.RCBuggy.PowerHBridgeSpeedDriver import PowerHBridgeSpeedDriver
from components.drivers.RCBuggy.ServoSteeringDriver import ServoSteeringDriver
from components.drivers.ShelbyGT500.DCSteeringDriver import DCSteeringDriver
from components.drivers.ShelbyGT500.HBridgeSpeedDriver import HBridgeSpeedDriver
from components.drivers.PlatformEn import PlatformEn
import RPi.GPIO


class PiCarController:

    def __init__(self):
        self.__active_platform = PlatformEn.NONE
        # self.steer_driver = ServoSteeringDriver.get_instance()
        # self.speed_driver = PowerHBridgeSpeedDriver.get_instance()

        self.__steer_driver = DCSteeringDriver.get_instance()
        self.__speed_driver = HBridgeSpeedDriver.get_instance()
        return

    def activate_control(self, enable, requested_platform):
        enable = True if enable.lower() == "true" else False

        if self.__active_platform != requested_platform and self.__active_platform != PlatformEn.NONE:
            self.__speed_driver.enable_disable_driver(False)
            self.__steer_driver.enable_disable_driver(False)
            RPi.GPIO.cleanup()
            print('Closed PiCarController with all associated GPIO channels')

        self.__active_platform = requested_platform

        if requested_platform == PlatformEn.RCBUGGY:
            self.__steer_driver = ServoSteeringDriver.get_instance()
            self.__speed_driver = PowerHBridgeSpeedDriver.get_instance()
        elif requested_platform == PlatformEn.SHELBYGT500:
            self.__steer_driver = DCSteeringDriver.get_instance()
            self.__speed_driver = HBridgeSpeedDriver.get_instance()

        self.__speed_driver.enable_disable_driver(enable)
        self.__steer_driver.enable_disable_driver(enable)
        if not enable:
            RPi.GPIO.cleanup()
            print('Closed PiCarController with all associated GPIO channels')
        return

    def request_speed(self, speed, direction):
        self.__speed_driver.set_speed(speed, direction)
        return

    def request_steering(self, steer, direction):
        self.__steer_driver.set_steering(steer, direction)
        return


# if __name__ == "__main__":
#     try:
#         stuff = PiCarController()
#         stuff.activate_control(True, PlatformEn.SHELBYGT500)
#         stuff.request_steering(40, 0)
#         stuff.request_speed(50, 1)
#     finally:
#         stuff.activate_control(False, PlatformEn.SHELBYGT500)
