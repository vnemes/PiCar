import threading
from simple_pid import PID
import time

from ..AbstractComponent import AbstractComponent
from ..drivers.sensors.UltrasonicSensor import UltrasonicSensor


class AdaptiveCruiseController(AbstractComponent):
    __instance = None
    TARGET_DISTANCE = 20.0  # cm
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

    def __pid_control_speed(self):
        # TODO refine PID parameters
        # self.pid = PID(-1, 0.1, 0.05, setpoint=self.TARGET_DISTANCE)
        self.pid = PID(-4, -0.2, -1, setpoint=self.TARGET_DISTANCE)
        from components.PiCarController import PiCarController
        controller = PiCarController.get_instance()
        ultrasonic = UltrasonicSensor.get_instance()
        ultrasonic.enable_disable_driver(True)
        self.pid.sample_time = 1.0 / ultrasonic.DISTANCE_SAMPLING_FREQ
        self.pid.output_limits = (-100, 100)
        # self.pid.setpoint(self.TARGET_DISTANCE)
        # self.pid.Ki = 1.0
        # self.pid.tunings = (1.0, 0.2, 0.4)
        # self.pid.proportional_on_measurement = True
        time.sleep(1)  # wait for the sensor measurement to settle
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            dist = ultrasonic.get_data()
            next_speed = self.pid(dist)
            controller.middleware_set_speed(1 if next_speed > 0 else 0, abs(next_speed))
            print('PID: dist: %.2f - pwm: %.2f' % (dist, next_speed))
            time.sleep(self.pid.sample_time)
        return

    def start(self):
        if not self.pid_thread or not self.pid_thread.is_alive():
            self.pid_thread = threading.Thread(target=self.__pid_control_speed)
            self.pid_thread.daemon = True
            self.pid_thread.start()
        return

    def stop(self):
        if self.pid_thread and self.pid_thread.is_alive():
            self.pid_thread.THREAD_RUN_REQUESTED = False
            self.pid_thread.join()
            print('Closed Adaptive Cruise Controller')
        return
