import RPi.GPIO as gpio
from ..IDriver import IDriver


class HBridgeSpeedDriver(IDriver):

    __instance = None
    BCM_PIN_SPEED_HIGH = 19
    BCM_PIN_SPEED_LOW = 16
    GPIO_PWM_FREQUENCY = 20000  # 20kHz
    GPIO_TORQUE_CORRECT = 50

    @staticmethod
    def get_instance():
        """ Static access method. """
        if HBridgeSpeedDriver.__instance is None:
            HBridgeSpeedDriver()
        return HBridgeSpeedDriver.__instance

    def __init__(self):
        if HBridgeSpeedDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            HBridgeSpeedDriver.__instance = self
        return

    def set_speed(self, direction, speed):
        speed = self.__normalize(speed)

        self.speed_forward_pwm.ChangeDutyCycle(speed if direction else 0)
        self.speed_backward_pwm.ChangeDutyCycle(0 if direction else speed)
        print('speed:\t' + str(speed) + (' forward' if direction == 1 else ' backward'))
        return

    def __normalize(self, value):
        if value:
            value = self.GPIO_TORQUE_CORRECT + value / 2
        return value

    def start(self):
        gpio.setmode(gpio.BCM)
        output_channels = [self.BCM_PIN_SPEED_HIGH, self.BCM_PIN_SPEED_LOW]
        gpio.setup(output_channels, gpio.OUT)
        self.speed_forward_pwm = gpio.PWM(self.BCM_PIN_SPEED_HIGH, self.GPIO_PWM_FREQUENCY)
        self.speed_backward_pwm = gpio.PWM(self.BCM_PIN_SPEED_LOW, self.GPIO_PWM_FREQUENCY)
        self.speed_forward_pwm.start(0)
        self.speed_backward_pwm.start(0)
        print('Initialized HBridgeSpeedDriver with PWM frequency of ' + str(self.GPIO_PWM_FREQUENCY) + ' Hz')
        return

    def stop(self):
        self.speed_forward_pwm.stop()
        self.speed_backward_pwm.stop()
        print('Closed HBridgeSpeedDriver handler')
        return
