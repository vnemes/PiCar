import pigpio
from components.drivers.AbstractComponent import AbstractComponent


class HBridgeSpeedDriver(AbstractComponent):
    __instance = None
    BCM_PIN_SPEED_HIGH = 19
    BCM_PIN_SPEED_LOW = 16
    GPIO_PWM_FREQUENCY = 32000  # 20kHz
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
            self.pi = None
        return

    def set_speed(self, direction, speed):
        speed = self.__normalize(speed)
        self.set_speed_drv(direction, speed)
        return

    def set_speed_drv(self, direction, speed):
        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_HIGH, speed if direction else 0)
        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_LOW, 0 if direction else speed)
        print('speed:\t' + str(speed) + (' forward' if direction == 1 else ' backward'))
        return

    def __normalize(self, value):
        if value:
            value = self.GPIO_TORQUE_CORRECT + value / 2
        return value

    def start(self):
        self.pi = pigpio.pi()
        self.pi.set_PWM_range(self.BCM_PIN_SPEED_HIGH, 100)
        self.pi.set_PWM_range(self.BCM_PIN_SPEED_LOW, 100)
        self.pi.set_PWM_frequency(self.BCM_PIN_SPEED_HIGH, self.GPIO_PWM_FREQUENCY)
        self.pi.set_PWM_frequency(self.BCM_PIN_SPEED_LOW, self.GPIO_PWM_FREQUENCY)
        print('Initialized HBridgeSpeedDriver with PWM frequency of ' + str(self.GPIO_PWM_FREQUENCY) + ' Hz')
        return

    def stop(self):
        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_HIGH, 0)
        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_LOW, 0)
        self.pi.stop()
        self.pi = None
        print('Closed HBridgeSpeedDriver handler')
        return
