import RPi.GPIO as gpio
import time

class UltrasonicSensor:

    BCM_PIN_TRIG            = 6
    BCM_PIN_ECHO            = 12
    SOUND_SPEED_CONSTANT    = 17150
    GROUND                  = 0

    def __init__(self):
        gpio.setmode(gpio.BCM)

        gpio.setup(self.BCM_PIN_TRIG,gpio.OUT)
        gpio.setup(self.BCM_PIN_ECHO,gpio.IN)

        gpio.output(self.BCM_PIN_TRIG,False)
        print('Waiting for Ultrasonic Sensor to settle..')
        time.sleep(2)
        self.shouldIRun = True
        self.distance = None

        print('Initialized GPIO for the Ultrasonic Sensor')


    def start_measurement(self):
        while self.shouldIRun:
            gpio.output(self.BCM_PIN_TRIG,True)
            time.sleep(0.00001)
            gpio.output(self.BCM_PIN_TRIG,False)
            while gpio.input(self.BCM_PIN_ECHO) == 0:
                pulse_start = time.time()
            while gpio.input(self.BCM_PIN_ECHO) == 1:
                pulse_end = time.time()

            pulse_duration = pulse_end - pulse_start
            dist = pulse_duration * self.SOUND_SPEED_CONSTANT
            self.distance = round(dist,2)
            # print('computed distance: ' + str(self.distance) + ' cm')
            time.sleep(0.2)

    def get_distance(self):
        return self.distance


    def stop(self):
        self.shouldIRun = False
        time.sleep(0.3)
        gpio.cleanup()
        print('Closed GPIO for Ultrasonic Sensor')



#GPIO Ultrasonic Sensor Test: Expect various readings depending on the distance to the sensor itself
# import _thread
# if __name__ == "__main__":
#     try:
#         dut = UltrasonicSensor()
#         _thread.start_new_thread(dut.start_measurement,())
#         while True:
#             print(str(dut.get_distance()) + ' cm')
#             time.sleep(0.2)
#
#     finally:
#         dut.stop()
