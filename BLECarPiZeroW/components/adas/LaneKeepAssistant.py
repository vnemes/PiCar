import threading
import time

from components.adas.AbstractRefComponent import AbstractRefComponent
from components.core.PiRevEn import PiRevEn
from components.drivers.sensors.JeVoisDriver import JeVoisDriver


class LaneKeepAssistant(AbstractRefComponent):
    __instance = None
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if LaneKeepAssistant.__instance is None:
            LaneKeepAssistant()
        return LaneKeepAssistant.__instance

    def __init__(self):
        if LaneKeepAssistant.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            LaneKeepAssistant.__instance = self
            self.pid_thread = None
            self.pid = None
        return

    def __control_steering(self, steer_driver):
        platform = PiRevEn.detect_platform()
        if platform == PiRevEn.PI3B_PLUS:
            jevois = JeVoisDriver.get_instance()
        else:
            raise Exception('Cannot initialize LKA due to invalid platform!')

        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            vanishing_point = jevois.get_data()
            steer = 0
            if not vanishing_point:
                time.sleep(1)
                continue
            if vanishing_point < -1000 or vanishing_point > 1000:
                steer_driver.set_steering(0, steer)
            else:
                steer = vanishing_point / 10
                steer_driver.set_steering(1 if vanishing_point > 0 else 0, steer)
            print('LKA: vp: %.2f - steer: %.2f' % (vanishing_point, steer))
            time.sleep(0.1)
        steer_driver.set_steering(0, 0)
        return

    def start(self, steer_driver):
        if not self.pid_thread or not self.pid_thread.is_alive():
            self.pid_thread = threading.Thread(target=self.__control_steering, args=[steer_driver])
            self.pid_thread.daemon = True
            self.pid_thread.start()
        return

    def stop(self, caller):
        if self.pid_thread and self.pid_thread.is_alive():
            self.pid_thread.THREAD_RUN_REQUESTED = False
            self.pid_thread.join()
            print('Closed Lane Keep Assistant')
        JeVoisDriver.get_instance().stop()
        return


if __name__ == '__main__':
    try:
        import rpyc
        lka = LaneKeepAssistant.get_instance()
        driver = rpyc.connect_by_service("STEERINGDRIVER")
        lka.enable_disable_driver(True, driver.root)
        time.sleep(30)
    finally:
        lka.enable_disable_driver(False, None)
