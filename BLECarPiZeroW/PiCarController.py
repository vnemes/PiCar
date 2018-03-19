import pigpio

BCM_PIN_STEERING_HIGH   = 19
BCM_PIN_STEERING_LOW    = 16
BCM_PIN_SPEED_HIGH      = 26
BCM_PIN_SPEED_LOW       = 20
GPIO_PWM_FREQUENCY      = 20000 #20kHz
GROUND                  = 0


car = None

def car_init():
    global car
    #set pins 19, 16, 26, 20 to a pwm frequency of 20000
    car = pigpio.pi()
    car.set_PWM_range(BCM_PIN_STEERING_HIGH,100)
    car.set_PWM_range(BCM_PIN_STEERING_LOW,100)
    car.set_PWM_range(BCM_PIN_SPEED_HIGH,100)
    car.set_PWM_range(BCM_PIN_SPEED_LOW,100)
    car.set_PWM_frequency(BCM_PIN_STEERING_HIGH, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_STEERING_LOW, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_SPEED_HIGH, GPIO_PWM_FREQUENCY)
    car.set_PWM_frequency(BCM_PIN_SPEED_LOW, GPIO_PWM_FREQUENCY)
    print('Initialized GPIO with PWM frequency: ' + str(car.get_PWM_frequency(19)) + 'Hz') #debug information
    return

def car_setspeed(direction, speed):
    global car
    if direction == 1:
        car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH,speed)
        car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, GROUND)
        print('speed: ' + speed + ' forward')
    else:
        car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH,GROUND)
        car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, speed)
        print('speed: ' + speed + ' backward')

def car_setsteering(direction, steering):
    global car
    if direction == 1:
        car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH, steering)
        car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, GROUND)
        print('steering:' + steering + ' left')
    else:
        car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH, GROUND)
        car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, steering)
        print('steering: ' + steering + ' right')


def car_cleanup():
    global car

    car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH,GROUND)
    car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, GROUND)
    car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH,GROUND)
    car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, GROUND)

    car.stop()
    print('Closed GPIO handler')
    return
