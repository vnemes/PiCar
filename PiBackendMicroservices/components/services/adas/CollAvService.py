import rpyc

from components.adas.CollisionAvoidanceSystem import CollisionAvoidanceSystem
from components.services.AbstractComponentService import AbstractComponentService


class CollAvService(rpyc.Service, AbstractComponentService):

    ALIASES = ["CollisionAvoidance"]

    def on_connect(self, conn):
        self.colav = CollisionAvoidanceSystem.get_instance()

    def on_disconnect(self, conn):
        self.exposed_stop()

    def exposed_start(self):
        __speed_service = rpyc.connect_by_service("SpeedDriver")
        speed = __speed_service.root
        self.colav.enable_disable_driver(True, speed)
        self.started = True

    def exposed_stop(self):
        self.colav.enable_disable_driver(False, None)
        self.started = False


if __name__ == "__main__":
    from rpyc.utils.server import ThreadedServer

    server = ThreadedServer(CollAvService(), port=11132, auto_register=True)
    server.start()
