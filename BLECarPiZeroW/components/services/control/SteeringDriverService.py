from components.core.PiRevEn import PiRevEn
from components.drivers.RCBuggy.ServoSteeringDriver import ServoSteeringDriver
from components.drivers.ShelbyGT500.DCSteeringDriver import DCSteeringDriver
from components.services.AbstractComponentService import AbstractComponentService
import rpyc


class SteeringDriverService(rpyc.Service, AbstractComponentService):

    ALIASES = ["SteeringDriver", "DCSteeringDriver", "ServoSteeringDriver"]

    def on_connect(self, conn):
        platform = PiRevEn.detect_platform()
        if platform == PiRevEn.PIZEROW:
            self.driver = DCSteeringDriver.get_instance()
        elif platform == PiRevEn.PI3B_PLUS:
            self.driver = ServoSteeringDriver.get_instance()

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        self.driver.enable_disable_driver(True)
        self.started = True

    def exposed_stop(self):
        self.driver.enable_disable_driver(False)
        self.started = False

    def exposed_set_steering(self, direction, steering):
        self.driver.set_steering(direction, steering)



if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(SteeringDriverService(), port=11112, auto_register=True)
    server.start()
