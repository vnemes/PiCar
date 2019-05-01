from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
import RPi.GPIO
import rpyc

from components.adas.CollisionAvoidanceSystem import CollisionAvoidanceSystem
from components.services.AbstractComponentService import AbstractComponentService


class PiCarControllerService(rpyc.Service, AbstractComponentService):
    ALIASES = ["PiCarController", "Controller"]

    def on_connect(self, conn):
        pass

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        self.sp = rpyc.connect_by_service("SPEEDDRIVER")
        self.st = rpyc.connect_by_service("STEERINGDRIVER")
        self.__speed_driver = self.sp.root
        self.__steer_driver= self.st.root
        self.__speed_driver.enable_disable_driver(True)
        self.__steer_driver.enable_disable_driver(True)

    def exposed_stop(self):
        self.__speed_driver.enable_disable_driver(False)
        self.__steer_driver.enable_disable_driver(False)
        self.sp.close()
        self.st.close()
        RPi.GPIO.cleanup()
        print('Closed PiCarController with all associated GPIO channels')

    def exposed_set_speed_limit(self, limit):
        self.__speed_driver.change_speed_limit(limit)
        return

    def exposed_get_speed_status(self):
        return self.__speed_driver.get_status() if self.__speed_driver else False

    def exposed_get_steering_status(self):
        return self.__steer_driver.get_status() if self.__steer_driver else False

    def exposed_middleware_set_speed(self, direction, speed):
        self.__speed_driver.set_speed(direction, speed)
        return

    def exposed_request_speed(self, direction, speed):
        self.__speed_driver.set_speed(direction, speed)
        return

    def exposed_request_steering(self, direction, steer):
        self.__steer_driver.set_steering(direction, steer)
        return

if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(PiCarControllerService(), port=11110, auto_register=True)
    server.start()

