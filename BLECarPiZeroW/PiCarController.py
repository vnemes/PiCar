import pigpio

BCM_PIN_STEERING_HIGH   = 19
BCM_PIN_STEERING_LOW    = 16
BCM_PIN_SPEED_HIGH      = 26
BCM_PIN_SPEED_LOW       = 20
GPIO_PWM_FREQUENCY      = 20000 #20kHz


car = None

def car_init():
    global car
    #set pins 19, 16, 26, 20 to a pwm frequency of 20000
    car = pigpio.pi()
    car.set_PWM_frequency(BCM_PIN_STEERING_HIGH, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_STEERING_LOW, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_SPEED_HIGH, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_SPEED_LOW, GPIO_PWM_FREQUENCY)
    print('Initialized GPIO with PWM frequency: ' + str(car.get_PWM_frequency(19)) + 'Hz') #debug information
    return

def car_setspeed(speed):
#     if speed[0]:
#         car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH, speed[1])
#         car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, 0)
#     else:
#         car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH, 0)
#         car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, speed[1])
    print(repr(speed))

    return

def car_setsteering(steering):
    #
    # car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH, steering)
    # car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, steering)
    #
    print(repr(steering))
    return


def car_cleanup():
    global car

    car.stop()
    print('Closed GPIO handler')
    return
#
# car_init()
# car_cleanup()