from threading import Timer

from components.drivers.AbstractComponent import AbstractComponent


class WatchDog(AbstractComponent):
    __instance = None
    TIMEOUT = 5  # seconds

    @staticmethod
    def get_instance():
        """ Static access method. """
        if WatchDog.__instance is None:
            WatchDog()
        return WatchDog.__instance

    def __init__(self):
        if WatchDog.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            WatchDog.__instance = self
            self.timer = None
        pass

    def start(self):
        self.enable_disable_driver(False)
        self.timer = Timer(self.TIMEOUT, self.__handle_watchdog_expiry)
        self.timer.start()
        pass

    def stop(self):
        if self.timer:
            self.timer.cancel()
        pass

    def reset(self):
        self.enable_disable_driver(True)

    @staticmethod
    def __handle_watchdog_expiry():
        import rpyc
        __speed_service = rpyc.connect_by_service("SpeedDriver")
        __steer_service = rpyc.connect_by_service("SteeringDriver")
        speed_driver = __speed_service.root
        steer_driver = __steer_service.root
        speed_driver.set_speed(0, 0)
        steer_driver.set_steering(0, 0)
        speed_driver.enable_disable_driver(False)
        steer_driver.enable_disable_driver(False)



