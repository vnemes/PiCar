import RPi.GPIO as gpio
import time
import threading
from components.drivers.AbstractComponent import AbstractComponent


class UltrasonicSensor(AbstractComponent):
    __instance = None
    BCM_PIN_TRIG = 6
    BCM_PIN_ECHO = 12
    SOUND_SPEED_CONSTANT = 17150
    DISTANCE_SAMPLING_FREQ = 33  # Hz
    MEDIAN_FILTER_SIZE = 5
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
            self.raw_data = None
            self.ser = None
            self.lock = threading.Lock()
        return

    def __collect_data(self):
        t = threading.current_thread()
        sample_vect = [0] * self.MEDIAN_FILTER_SIZE
        sample_idx = 0
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            gpio.output(self.BCM_PIN_TRIG, True)
            time.sleep(0.00001)
            gpio.output(self.BCM_PIN_TRIG, False)
            start = time.time()
            pulse_start = time.time()  # timeout strategy in case of missed response
            while gpio.input(self.BCM_PIN_ECHO) == 0 and pulse_start - start < 0.300:
                pulse_start = time.time()
            if pulse_start >= start + 0.300:
                continue

            start = time.time()
            pulse_end = time.time()  # timeout strategy in case of missed response
            while gpio.input(self.BCM_PIN_ECHO) == 1 and pulse_end - start < 0.300:
                pulse_end = time.time()
            if pulse_end >= start + 0.300:
                continue

            if pulse_start == 0 or pulse_end == 0:
                continue

            pulse_duration = pulse_end - pulse_start
            dist = pulse_duration * self.SOUND_SPEED_CONSTANT
            sample_vect[sample_idx % self.MEDIAN_FILTER_SIZE] = dist
            sample_idx += 1
            with self.lock:
                self.raw_data = round(dist, 2)
            dist = sorted(sample_vect)[self.MEDIAN_FILTER_SIZE // 2]
            with self.lock:
                self.latest_distance_value = round(dist)
            # print('computed distance: ' + str(self.latest_distance_value) + ' cm')
            time.sleep(1.0/self.DISTANCE_SAMPLING_FREQ)
        return

    def get_filtered_data(self):
        if not self.started:
            self.start()
        with self.lock:
            return self.latest_distance_value

    def get_data(self):
        if not self.started:
            self.start()
        with self.lock:
            return self.raw_data

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
