import RPi.GPIO as gpio
import time
import threading
from ..IDriver import IDriver


class UltrasonicSensor(IDriver):
    __instance = None
    BCM_PIN_TRIG = 6
    BCM_PIN_ECHO = 12
    SOUND_SPEED_CONSTANT = 17150
    DISTANCE_SAMPLING_FREQ = 500 # Hz
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if UltrasonicSensor.__instance is None:
            UltrasonicSensor()
        return UltrasonicSensor.__instance

    def __init__(self):
        if UltrasonicSensor.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            UltrasonicSensor.__instance = self
            self.data_thread = None
            self.latest_distance_value = None
            self.ser = None
        return

    def __collect_data(self):
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            gpio.output(self.BCM_PIN_TRIG, True)
            time.sleep(0.00001)
            gpio.output(self.BCM_PIN_TRIG, False)
            start = time.time()
            stop = time.time()  # timeout strategy in case of missed response
            while gpio.input(self.BCM_PIN_ECHO) == 0 and stop - start < 300:
                pulse_start = time.time()
                stop = time.time()
            if stop >= start + 300:
                self.latest_distance_value = None
                continue

            start = time.time()
            stop = time.time()  # timeout strategy in case of missed response
            while gpio.input(self.BCM_PIN_ECHO) == 1 and stop - start < 300:
                pulse_end = time.time()
                stop = time.time()
            if stop >= start + 300:
                self.latest_distance_value = None
                continue

            pulse_duration = pulse_end - pulse_start
            dist = pulse_duration * self.SOUND_SPEED_CONSTANT
            self.latest_distance_value = round(dist, 2)
            # print('computed distance: ' + str(self.distance) + ' cm')
            time.sleep(1.0/self.DISTANCE_SAMPLING_FREQ)
        return

    def get_data(self):
        return self.latest_distance_value

    def start(self):
        if not self.data_thread or not self.data_thread.is_alive():
            gpio.setmode(gpio.BCM)

            gpio.setup(self.BCM_PIN_TRIG, gpio.OUT)
            gpio.setup(self.BCM_PIN_ECHO, gpio.IN)

            gpio.output(self.BCM_PIN_TRIG, False)
            print('Waiting for Ultrasonic Sensor to settle..')
            time.sleep(2)
            self.latest_distance_value = None

            print('Initialized GPIO for the Ultrasonic Sensor')
            self.data_thread = threading.Thread(target=self.__collect_data)
            self.data_thread.daemon = True
            self.data_thread.start()
        return

    def stop(self):
        if self.data_thread and self.data_thread.is_alive():
            self.data_thread.THREAD_RUN_REQUESTED = False
            self.data_thread.join()
            print('Closed GPIO for Ultrasonic Sensor')
        return
