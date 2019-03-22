import abc


class AbstractRefComponent(abc.ABC):

    def enable_disable_driver(self, enable, caller):
        if enable:
            self.start(caller)
        else:
            self.stop(caller)
        return

    @abc.abstractmethod
    def start(self, caller):
        pass

    @abc.abstractmethod
    def stop(self, caller):
        pass
