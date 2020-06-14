import abc


class AbstractComponent(abc.ABC):

    started = False

    def enable_disable_driver(self, enable):
        if enable:
            self.start()
            self.started = True
        else:
            self.stop()
            self.started = False
        return

    def get_status(self):
        return self.started

    @abc.abstractmethod
    def start(self):
        pass

    @abc.abstractmethod
    def stop(self):
        pass
