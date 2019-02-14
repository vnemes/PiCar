import RPi.GPIO as gpio
from ..IDriver import IDriver


class DCSteeringDriver(IDriver):

    __instance = None
    BCM_PIN_STEERING_HIGH = 26
    BCM_PIN_STEERING_LOW = 20
    GPIO_PWM_FREQUENCY = 20000  # 20kHz
    GPIO_TORQUE_CORRECT = 50

    @staticmethod
    def get_instance():
        """ Static access method. """
        if DCSteeringDriver.__instance is None:
            DCSteeringDriver()
        return DCSteeringDriver.__instance

    def __init__(self):
        if DCSteeringDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            DCSteeringDriver.__instance = self
        return

    def set_steering(self, direction, steering):
        steering = self.__normalize(steering)

        self.steer_left_pwm.ChangeDutyCycle(steering if direction else 0)
        self.steer_right_pwm.ChangeDutyCycle(0 if direction else steering)
        print('steer:\t' + str(steering) + (' left' if direction == 1 else ' right'))
        return

    def __normalize(self, value):
        if value:
            value = self.GPIO_TORQUE_CORRECT + value / 2
        return value

    def start(self):
        gpio.setmode(gpio.BCM)
        output_channels = [self.BCM_PIN_STEERING_HIGH, self.BCM_PIN_STEERING_LOW]
        gpio.setup(output_channels, gpio.OUT)
        self.steer_left_pwm = gpio.PWM(self.BCM_PIN_STEERING_HIGH, self.GPIO_PWM_FREQUENCY)
        self.steer_right_pwm = gpio.PWM(self.BCM_PIN_STEERING_LOW, self.GPIO_PWM_FREQUENCY)
        self.steer_left_pwm.start(0)
        self.steer_right_pwm.start(0)
        print('Initialized DCSteeringDriver with PWM frequency of ' + str(self.GPIO_PWM_FREQUENCY) + ' Hz')
        return

    def stop(self):
        self.steer_left_pwm.stop()
        self.steer_right_pwm.stop()
        print('Closed DCSteeringDriver handler')
        return
