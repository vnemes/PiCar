import math
import threading

import rpyc
from simple_pid import PID
import time

from components.adas.AbstractRefComponent import AbstractRefComponent
from components.core.PiRevEn import PiRevEn
from ..drivers.sensors.UltrasonicSensor import UltrasonicSensor


class AdaptiveCruiseController(AbstractRefComponent):
    __instance = None
    TARGET_DISTANCE = 50.0  # cm
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if AdaptiveCruiseController.__instance is None:
            AdaptiveCruiseController()
        return AdaptiveCruiseController.__instance

    def __init__(self):
        if AdaptiveCruiseController.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            AdaptiveCruiseController.__instance = self
            self.pid_thread = None
            self.pid = None
        return

    def __pid_control_speed(self, speed_driver):
        platform = PiRevEn.detect_platform()
        if platform == PiRevEn.PIZEROW:
            self.pid = PID(-4, -0.2, -1, setpoint=self.TARGET_DISTANCE)
            ultrasonic = UltrasonicSensor.get_instance()
        elif platform == PiRevEn.PI3B_PLUS:
            # self.pid = PID(-0.5, -0.1, -0.25, setpoint=self.TARGET_DISTANCE)
            self.pid = PID(-0.1, 0, -0.015, setpoint=self.TARGET_DISTANCE)
            ultrasonic_service = rpyc.connect_by_service("UltrasonicSensor")
            ultrasonic = ultrasonic_service.root
        else:
            raise Exception('Cannot initialize ACC due to invalid platform!')

        self.pid.sample_time = 1.0 / ultrasonic.DISTANCE_SAMPLING_FREQ
        self.pid.output_limits = (-100, 100)
        time.sleep(1)  # wait for the sensor measurement to settle
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            dist = ultrasonic.get_filtered_data()
            next_speed = round(self.pid(dist))
            speed_driver.set_speed(1 if next_speed > 0 else 0, math.floor(abs(next_speed)))
            print('PID: dist: %.2f - pwm: %.2f' % (dist, next_speed))
            time.sleep(self.pid.sample_time)
        speed_driver.set_speed(0, 0)
        return

    def start(self, speed_driver):
        if not self.pid_thread or not self.pid_thread.is_alive():
            self.pid_thread = threading.Thread(target=self.__pid_control_speed, args=[speed_driver])
            self.pid_thread.daemon = True
            self.pid_thread.start()
        return

    def stop(self, caller):
        if self.pid_thread and self.pid_thread.is_alive():
            self.pid_thread.THREAD_RUN_REQUESTED = False
            self.pid_thread.join()
            print('Closed Adaptive Cruise Controller')
        return
