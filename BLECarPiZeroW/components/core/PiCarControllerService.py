from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
import RPi.GPIO
import rpyc

from components.services.AbstractComponentService import AbstractComponentService


class PiCarControllerService(rpyc.Service, AbstractComponentService):
    ALIASES = ["PiCarController", "Controller"]

    def on_connect(self, conn):
        pass

    def on_disconnect(self, conn):
        self.exposed_activate_cruise_control(False)
        self.exposed_stop()

    def exposed_start(self):
        self.sp = rpyc.connect_by_service("SPEEDDRIVER")
        self.st = rpyc.connect_by_service("STEERINGDRIVER")
        self.__speed_driver = self.sp.root
        self.__steer_driver= self.st.root
        self.__speed_driver.enable_disable_driver(True)
        self.__steer_driver.enable_disable_driver(True)


    def exposed_stop(self):
        self.exposed_activate_cruise_control(False)
        self.__speed_driver.enable_disable_driver(False)
        self.__steer_driver.enable_disable_driver(False)
        self.sp.close()
        self.st.close()
        RPi.GPIO.cleanup()
        print('Closed PiCarController with all associated GPIO channels')

    def exposed_activate_cruise_control(self, enable):
        self.cruise_controller = AdaptiveCruiseController.get_instance()
        self.cruise_controller.enable_disable_driver(enable, self)
        return

    def middleware_set_speed(self, direction, speed):
        self.__speed_driver.set_speed(direction, speed)
        return

    def exposed_request_speed(self, direction, speed):
        self.exposed_activate_cruise_control(False)
        self.__speed_driver.set_speed(direction, speed)
        return

    def exposed_request_steering(self, direction, steer):
        self.exposed_activate_cruise_control(False)
        self.__steer_driver.set_steering(direction, steer)
        return

if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(PiCarControllerService(), port=11110, auto_register=True)
    server.start()

