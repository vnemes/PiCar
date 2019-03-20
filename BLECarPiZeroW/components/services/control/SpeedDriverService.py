from components.core.PiRevEn import PiRevEn
from components.drivers.RCBuggy.ESCSpeedDriver import ESCSpeedDriver
from components.drivers.ShelbyGT500.HBridgeSpeedDriver import HBridgeSpeedDriver
from components.services.AbstractComponentService import AbstractComponentService
import rpyc


class SpeedDriverService(rpyc.Service, AbstractComponentService):

    ALIASES = ["SpeedDriver", "HBridgeSpeedDriver", "ESCSpeedDriver"]

    def on_connect(self, conn):
        platform = PiRevEn.detect_platform()
        if platform == PiRevEn.PIZEROW:
            self.driver = HBridgeSpeedDriver.get_instance()
        elif platform == PiRevEn.PI3B_PLUS:
            self.driver = ESCSpeedDriver.get_instance()

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        self.driver.enable_disable_driver(True)
        self.started = True

    def exposed_stop(self):
        self.driver.enable_disable_driver(False)
        self.started = False

    def exposed_set_speed(self, direction, speed):
        self.driver.set_speed(direction, speed)


if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(SpeedDriverService(), port=11111, auto_register=True)
    server.start()
