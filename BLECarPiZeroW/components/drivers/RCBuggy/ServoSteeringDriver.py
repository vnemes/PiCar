import RPi.GPIO as gpio
from components.AbstractComponent import AbstractComponent


class ServoSteeringDriver(AbstractComponent):

    __instance = None
    BCM_PIN_STEERING = 26
    STEERING_PWM_FREQUENCY = 50  # Hz

    @staticmethod
    def get_instance():
        """ Static access method. """
        if ServoSteeringDriver.__instance is None:
            ServoSteeringDriver()
        return ServoSteeringDriver.__instance

    def __init__(self):
        if ServoSteeringDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            ServoSteeringDriver.__instance = self
        return

    def set_steering(self, direction, steering):
        _STEER_TO_PWIDTH_RATIO = 0.025
        _90_DEG_IN_PWIDTH = 7.5
        steering_displacement = steering * _STEER_TO_PWIDTH_RATIO
        steering_displacement -= 2 * direction * steering_displacement  # invert on direction
        steering_pulse_width = _90_DEG_IN_PWIDTH + steering_displacement
        self.steering_pwm.ChangeDutyCycle(steering_pulse_width)
        print('steer:\t' + str(steering) + (' right' if direction == 1 else ' left') + ' pwm: ' + str(
            steering_pulse_width))

    def start(self):
        gpio.setmode(gpio.BCM)
        gpio.setup(self.BCM_PIN_STEERING, gpio.OUT)
        self.steering_pwm = gpio.PWM(self.BCM_PIN_STEERING, self.STEERING_PWM_FREQUENCY)
        self.steering_pwm.start(0)
        print('Initialized ServoSteeringDriver with PWM frequency of ' + str(self.STEERING_PWM_FREQUENCY) + ' Hz')

    def stop(self):
        self.steering_pwm.stop()
        print('Closed ServoSteeringDriver handler')
