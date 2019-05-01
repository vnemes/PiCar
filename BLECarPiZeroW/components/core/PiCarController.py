from components.drivers.RCBuggy.ESCSpeedDriver import ESCSpeedDriver
from components.drivers.RCBuggy.ServoSteeringDriver import ServoSteeringDriver
from components.drivers.ShelbyGT500.DCSteeringDriver import DCSteeringDriver
from components.drivers.ShelbyGT500.HBridgeSpeedDriver import HBridgeSpeedDriver
from components.core.PlatformEn import PlatformEn
from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
import RPi.GPIO


class PiCarController:
    __instance = None

    @staticmethod
    def get_instance():
        """ Static access method. """
        if PiCarController.__instance is None:
            PiCarController()
        return PiCarController.__instance

    def __init__(self):
        if PiCarController.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            PiCarController.__instance = self
            self.__active_platform = PlatformEn.NONE
            self.__speed_driver = None
            self.__steer_driver = None
            self.cruise_controller = AdaptiveCruiseController.get_instance()
        return

    def activate_control(self, enable, requested_platform):

        if self.__active_platform != requested_platform and self.__active_platform != PlatformEn.NONE:
            self.__speed_driver.enable_disable_driver(False)
            self.__steer_driver.enable_disable_driver(False)
            RPi.GPIO.cleanup()
            print('Closed PiCarController with all associated GPIO channels')

        self.__active_platform = requested_platform

        if requested_platform == PlatformEn.RCBUGGY:
            self.__steer_driver = ServoSteeringDriver.get_instance()
            self.__speed_driver = ESCSpeedDriver.get_instance()
        elif requested_platform == PlatformEn.SHELBYGT500:
            self.__steer_driver = DCSteeringDriver.get_instance()
            self.__speed_driver = HBridgeSpeedDriver.get_instance()

        self.__speed_driver.enable_disable_driver(enable)
        self.__steer_driver.enable_disable_driver(enable)
        if not enable:
            RPi.GPIO.cleanup()
            print('Closed PiCarController with all associated GPIO channels')
        return

    def middleware_set_speed(self, direction, speed):
        self.__speed_driver.set_speed(direction, speed)
        return

    def set_speed_limit(self, limit):
        self.__speed_driver.change_speed_limit(limit)
        return

    def get_speed_status(self):
        return self.__speed_driver.get_status() if self.__speed_driver else False

    def get_steering_status(self):
        return self.__steer_driver.get_status() if self.__steer_driver else False

    def request_speed(self, direction, speed):
        self.__speed_driver.set_speed(direction, speed)
        return

    def request_steering(self, direction, steer):
        self.__steer_driver.set_steering(direction, steer)
        return

