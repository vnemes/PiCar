import abc


class AbstractComponentService(abc.ABC):

    def exposed_enable_disable_driver(self, enable):
        if enable:
            self.exposed_start()
        else:
            self.exposed_stop()
        return

    def exposed_get_status(self):
        return self.started

    @abc.abstractmethod
    def exposed_start(self):
        pass

    @abc.abstractmethod
    def exposed_stop(self):
        pass
