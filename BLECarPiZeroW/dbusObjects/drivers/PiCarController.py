import pigpio

class PiCarController:

    BCM_PIN_SPEED_HIGH      = 19
    BCM_PIN_SPEED_LOW       = 16
    BCM_PIN_STEERING_HIGH   = 26
    BCM_PIN_STEERING_LOW    = 20
    GPIO_PWM_FREQUENCY      = 20000 #20kHz
    GPIO_TORQUE_CORRECT     = 50
    GPIO_GROUND             = 0

    def __init__(self):
        self.pi = pigpio.pi()
        self.pi.set_PWM_range(self.BCM_PIN_STEERING_HIGH, 100)
        self.pi.set_PWM_range(self.BCM_PIN_STEERING_LOW, 100)
        self.pi.set_PWM_range(self.BCM_PIN_SPEED_HIGH, 100)
        self.pi.set_PWM_range(self.BCM_PIN_SPEED_LOW, 100)

        self.pi.set_PWM_frequency(self.BCM_PIN_STEERING_HIGH, self.GPIO_PWM_FREQUENCY)
        self.pi.set_PWM_frequency(self.BCM_PIN_STEERING_LOW, self.GPIO_PWM_FREQUENCY)
        self.pi.set_PWM_frequency(self.BCM_PIN_SPEED_HIGH, self.GPIO_PWM_FREQUENCY)
        self.pi.set_PWM_frequency(self.BCM_PIN_SPEED_LOW, self.GPIO_PWM_FREQUENCY)

        print('Initialized GPIO with PWM frequency of ' + str(self.pi.get_PWM_frequency(self.BCM_PIN_STEERING_HIGH)) + ' Hz')
        return

    def normalize(self,value):
        if value:
            value = self.GPIO_TORQUE_CORRECT + value/2
        return value


    def set_speed(self,direction, speed):
        speed = self.normalize(speed)
        if 0 <= speed <= 100:
            if direction:
                self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_HIGH, speed)
                self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_LOW, self.GPIO_GROUND)
                print('speed: ' + str(speed) + ' forward')
            else:

                self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_HIGH, self.GPIO_GROUND)
                self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_LOW, speed)
                print('speed: ' + str(speed) + ' backward')


    def set_steering(self,direction, steering):
        steering = self.normalize(steering)
        if 0 <= steering <= 100:
            if direction:
                self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_HIGH, steering)
                self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_LOW, self.GPIO_GROUND)
                print('speed: ' + str(steering) + ' left')
            else:

                self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_HIGH, self.GPIO_GROUND)
                self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_LOW, steering)
                print('speed: ' + str(steering) + ' right')


    def stop(self):

        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_HIGH, self.GPIO_GROUND)
        self.pi.set_PWM_dutycycle(self.BCM_PIN_SPEED_LOW, self.GPIO_GROUND)
        self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_HIGH, self.GPIO_GROUND)
        self.pi.set_PWM_dutycycle(self.BCM_PIN_STEERING_LOW, self.GPIO_GROUND)
        self.pi.stop()

        print('Closed GPIO handler')


#
# GPIO Test: Expect 1.27 V between BCM 26 and BCM 20 for 20 seconds
#
# import time
# if __name__ == "__main__":
#     try:
#         stuff = PiCarController()
#         stuff.set_speed(1,50)
#         time.sleep(20)
#     finally:
#         stuff.stop()
