import threading
from simple_pid import PID
import time

from components.adas.AbstractRefComponent import AbstractRefComponent
from components.core.PiRevEn import PiRevEn
from ..drivers.sensors.UltrasonicSensor import UltrasonicSensor


class CollisionAvoidanceSystem(AbstractRefComponent):
    __instance = None
    TARGET_DISTANCE = 30.0  # cm
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if CollisionAvoidanceSystem.__instance is None:
            CollisionAvoidanceSystem()
        return CollisionAvoidanceSystem.__instance

    def __init__(self):
        if CollisionAvoidanceSystem.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            CollisionAvoidanceSystem.__instance = self
            self.pid_thread = None
            self.pid = None
        return

    def __pid_control_speed(self, controller):
        platform = PiRevEn.detect_platform()
        if platform == PiRevEn.PIZEROW:
            self.pid = PID(-4, 0, 0, setpoint=self.TARGET_DISTANCE)
        elif platform == PiRevEn.PI3B_PLUS:
            self.pid = PID(-0.5, 0, 0, setpoint=self.TARGET_DISTANCE)
        else:
            raise Exception('Cannot initialize ACC due to invalid platform!')
        ultrasonic = UltrasonicSensor.get_instance()
        ultrasonic.enable_disable_driver(True)
        self.pid.sample_time = 1.0 / ultrasonic.DISTANCE_SAMPLING_FREQ
        self.pid.output_limits = (-100, 100)
        time.sleep(1)  # wait for the sensor measurement to settle
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            dist = ultrasonic.get_filtered_data()
            next_speed = self.pid(dist)
            if next_speed < 0:
                controller.middleware_set_speed(1 if next_speed > 0 else 0, round(abs(next_speed)))
                print('PID: dist: %.2f - pwm: %.2f' % (dist, next_speed))
            else:
                print('PID: dist :%.2f - no pwm' % dist)
            time.sleep(self.pid.sample_time)
        controller.middleware_set_speed(0, 0)
        return

    def start(self, caller):
        if not self.pid_thread or not self.pid_thread.is_alive():
            self.pid_thread = threading.Thread(target=self.__pid_control_speed, args=[caller])
            self.pid_thread.daemon = True
            self.pid_thread.start()
        return

    def stop(self, caller):
        if self.pid_thread and self.pid_thread.is_alive():
            self.pid_thread.THREAD_RUN_REQUESTED = False
            self.pid_thread.join()
            print('Closed Collision Avoidance System')
        return
