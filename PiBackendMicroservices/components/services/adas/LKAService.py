import rpyc

from components.adas.LaneKeepAssistant import LaneKeepAssistant
from components.services.AbstractComponentService import AbstractComponentService


class LKAService(rpyc.Service, AbstractComponentService):

    ALIASES = ["LaneKeepAssistant"]

    def on_connect(self, conn):
        self.lka = LaneKeepAssistant.get_instance()

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        __steer_service = rpyc.connect_by_service("STEERINGDRIVER")
        steer = __steer_service.root
        self.lka.enable_disable_driver(True, steer)
        self.started = True

    def exposed_stop(self):
        self.lka.enable_disable_driver(False, None)
        self.started = False


if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(LKAService(), port=11133, auto_register=True)
    server.start()
