import RPi.GPIO as gpio

class PiCarController:

    BCM_PIN_STEERING_HIGH   = 19
    BCM_PIN_STEERING_LOW    = 16
    BCM_PIN_SPEED_HIGH      = 26
    BCM_PIN_SPEED_LOW       = 20
    GPIO_PWM_FREQUENCY      = 20000 #20kHz
    GROUND                  = 0

    def __init__(self):
        gpio.setmode(gpio.BCM)
        #set pins 19, 16, 26, 20 to a pwm frequency of 20000
        gpio.setup(self.BCM_PIN_STEERING_HIGH,gpio.OUT)
        gpio.setup(self.BCM_PIN_STEERING_LOW,gpio.OUT)
        gpio.setup(self.BCM_PIN_SPEED_HIGH,gpio.OUT)
        gpio.setup(self.BCM_PIN_SPEED_LOW,gpio.OUT)

        self.pwm_forward  = gpio.PWM(self.BCM_PIN_STEERING_HIGH,self.GPIO_PWM_FREQUENCY)
        self.pwm_backward = gpio.PWM(self.BCM_PIN_STEERING_LOW,self.GPIO_PWM_FREQUENCY)
        self.pwm_left     = gpio.PWM(self.BCM_PIN_SPEED_HIGH,self.GPIO_PWM_FREQUENCY)
        self.pwm_right    = gpio.PWM(self.BCM_PIN_SPEED_LOW,self.GPIO_PWM_FREQUENCY)

        self.pwm_forward.start(self.GROUND)
        self.pwm_backward.start(self.GROUND)
        self.pwm_left.start(self.GROUND)
        self.pwm_right.start(self.GROUND)

        print('Initialized GPIO with PWM frequency 20 kHz')
        return

    def set_speed(self,direction, speed):
        if direction:
            self.pwm_forward.start(speed)
            self.pwm_backward.start(self.GROUND)
            print('speed: ' + str(speed) + ' forward')
        else:
            self.pwm_forward.start(self.GROUND)
            self.pwm_backward.start(speed)
            print('speed: ' + str(speed) + ' backward')

    def set_steering(self,direction, steering):
        if direction:
            self.pwm_left.start(steering)
            self.pwm_right.start(self.GROUND)
            print('steering:' + str(steering) + ' left')
        else:
            self.pwm_left.start(self.GROUND)
            self.pwm_right.start(steering)
            print('steering: ' + str(steering) + ' right')


    def stop(self):

        self.pwm_forward.stop()
        self.pwm_backward.stop()
        self.pwm_left.stop()
        self.pwm_right.stop()

        gpio.cleanup()
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
