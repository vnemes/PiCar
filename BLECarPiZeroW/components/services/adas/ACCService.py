import rpyc

from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
from components.services.AbstractComponentService import AbstractComponentService


class ACCService(rpyc.Service, AbstractComponentService):

    ALIASES = ["AdaptiveCruise"]

    def on_connect(self, conn):
        self.acc = AdaptiveCruiseController.get_instance()

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        __speed_service = rpyc.connect_by_service("SpeedDriver")
        speed = __speed_service.root
        self.acc.enable_disable_driver(True, speed)
        self.started = True

    def exposed_stop(self):
        self.acc.enable_disable_driver(False, None)
        self.started = False


if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(ACCService(), port=11131, auto_register=True)
    server.start()
