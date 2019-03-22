import RPi.GPIO as gpio
from components.drivers.AbstractComponent import AbstractComponent


class ESCSpeedDriver(AbstractComponent):

    __instance = None
    BCM_PIN_SPEED_PWM = 19
    BCM_PIN_SPEED_DIR = 16
    BCM_PIN_SPEED_EN = 20

    GPIO_PWM_FREQUENCY = 20000  # 20kHz
    GPIO_GROUND = 0

    @staticmethod
    def get_instance():
        """ Static access method. """
        if ESCSpeedDriver.__instance is None:
            ESCSpeedDriver()
        return ESCSpeedDriver.__instance

    def __init__(self):
        if ESCSpeedDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            ESCSpeedDriver.__instance = self
            self.speed_limit = 100
        return

    def change_speed_limit(self, limit):
        self.speed_limit = limit if limit <= 100 else 100

    def set_speed(self, direction, speed):
        self.set_speed_drv(direction, speed)

    def set_speed_drv(self, direction, speed):
        if speed > self.speed_limit:
            speed = self.speed_limit
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
