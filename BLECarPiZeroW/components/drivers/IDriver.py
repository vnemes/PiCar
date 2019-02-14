import abc


class IDriver(abc.ABC):

    def enable_disable_driver(self, enable):
        if enable:
            self.start()
        else:
            self.stop()
        return

    @abc.abstractmethod
    def start(self):
        pass

    @abc.abstractmethod
    def stop(self):
        pass
