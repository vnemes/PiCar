import RPi.GPIO as gpio


class PiCarController:
    BCM_PIN_SPEED_PWM = 19
    BCM_PIN_SPEED_DIR = 16
    BCM_PIN_SPEED_EN = 20
    BCM_PIN_STEERING = 26
    BCM_PIN_STEERING_LOW = 20
    GPIO_PWM_FREQUENCY = 20000  # 20kHz
    GPIO_TORQUE_CORRECT = 50
    GPIO_GROUND = 0

    def __init__(self):
        gpio.setmode(gpio.BCM)
        output_channels = [self.BCM_PIN_SPEED_PWM, self.BCM_PIN_SPEED_DIR, self.BCM_PIN_STEERING, self.BCM_PIN_SPEED_EN]
        gpio.setup(output_channels, gpio.OUT)
        self.speed_pwm = gpio.PWM(self.BCM_PIN_SPEED_PWM, self.GPIO_PWM_FREQUENCY)
        self.steering_pwm = gpio.PWM(self.BCM_PIN_STEERING, 50)
        gpio.output(self.BCM_PIN_SPEED_EN, gpio.HIGH)
        self.speed_pwm.start(0)
        self.steering_pwm.start(0)

        print('Initialized GPIO with PWM frequency of ' + str(self.GPIO_PWM_FREQUENCY) + ' Hz')
        return

    def normalize(self, value):
        if value:
            value = self.GPIO_TORQUE_CORRECT + value / 2
        return value

    def set_speed(self, direction, speed):

        self.speed_pwm.ChangeDutyCycle(speed)
        gpio.output(self.BCM_PIN_SPEED_DIR, gpio.HIGH if direction == 1 else gpio.LOW)
        print('speed: ' + str(speed) + ' forward' if direction == 1 else 'backward')

    def set_steering(self, direction, steering):
        
        _STEER_TO_PWIDTH_RATIO = 0.025
        _90_DEG_IN_PWIDTH = 7.5
        steering_displacement = steering * _STEER_TO_PWIDTH_RATIO
        steering_displacement -= 2 * direction * steering_displacement  # invert on direction
        steering_pulse_width = _90_DEG_IN_PWIDTH + steering_displacement
        self.steering_pwm.ChangeDutyCycle(steering_pulse_width)
        print('steering ' + str(steering) + (' right' if direction == 1 else ' left') + ' pwm: ' + str(
            steering_pulse_width))

    def stop(self):
        self.speed_pwm.stop()
        self.steering_pwm.stop()
        gpio.output(self.BCM_PIN_SPEED_EN, gpio.LOW)
        gpio.cleanup()
        print('Closed GPIO handler')

# if __name__ == "__main__":
#     try:
#         stuff = PiCarController()
#         while True:
#             dirr, steer = [float(x) for x in input("direction, steering: ").split()]
#             stuff.set_steering(dirr, steer)
#     finally:
#         stuff.stop()
