import RPi.GPIO as gpio
from ..IControlDriver import IControlDriver


class PowerHBridgeSpeedDriver(IControlDriver):

    __instance = None
    BCM_PIN_SPEED_PWM = 19
    BCM_PIN_SPEED_DIR = 16
    BCM_PIN_SPEED_EN = 20

    GPIO_PWM_FREQUENCY = 20000  # 20kHz
    GPIO_GROUND = 0

    @staticmethod
    def get_instance():
        """ Static access method. """
        if PowerHBridgeSpeedDriver.__instance is None:
            PowerHBridgeSpeedDriver()
        return PowerHBridgeSpeedDriver.__instance

    def __init__(self):
        if PowerHBridgeSpeedDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            PowerHBridgeSpeedDriver.__instance = self
        return

    def set_speed(self, direction, speed):
        self.speed_pwm.ChangeDutyCycle(speed)
        gpio.output(self.BCM_PIN_SPEED_DIR, gpio.HIGH if direction == 1 else gpio.LOW)
        print('speed:\t' + str(speed) + (' forward' if direction == 1 else ' backward'))
        return

    def start(self):
        gpio.setmode(gpio.BCM)
        output_channels = [self.BCM_PIN_SPEED_PWM, self.BCM_PIN_SPEED_DIR, self.BCM_PIN_SPEED_EN]
        gpio.setup(output_channels, gpio.OUT)
        self.speed_pwm = gpio.PWM(self.BCM_PIN_SPEED_PWM, self.GPIO_PWM_FREQUENCY)
        self.speed_pwm.start(0)
        gpio.output(self.BCM_PIN_SPEED_EN, gpio.HIGH)
        print('Initialized PowerHBridgeSpeedDriver with PWM frequency of ' + str(self.GPIO_PWM_FREQUENCY) + ' Hz')
        return

    def stop(self):
        self.speed_pwm.stop()
        gpio.output(self.BCM_PIN_SPEED_EN, gpio.LOW)
        print('Closed PowerHBridgeSpeedDriver handler')
        return
