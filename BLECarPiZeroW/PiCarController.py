import pigpio

BCM_PIN_STEERING_HIGH   = 19
BCM_PIN_STEERING_LOW    = 16
BCM_PIN_SPEED_HIGH      = 26
BCM_PIN_SPEED_LOW       = 20
GPIO_PWM_FREQUENCY      = 20000 #20kHz
GPIO_TORQUE_CORRECTION  = 128
GROUND                  = 0


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
    global car
    if speed == 0x80 or speed == 0x00: #direction is forward\backward, but speed is 0
        car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH,GROUND)
        car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, GROUND)
        print('speed: zero')
        return

    if ( speed & 0x80 ) == 0x80:    #if first bit is 1, direction is forward
        car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH, speed)
        car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, GROUND)
        print('speed: ' + str(speed) + ' forward')
    else:               #if first bit 0, direction is backward
        car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH, GROUND)
        car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, speed + GPIO_TORQUE_CORRECTION) # apply torque correction for backward direction
        print('speed: ' + str(speed + GPIO_TORQUE_CORRECTION) + ' backward')

def car_setsteering(steering):
    global car
    if steering == 0x80 or steering == 0x00: #direction is left/right, but speed is 0
        car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH,GROUND)
        car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, GROUND)
        print('steering: zero')
        return

    if ( steering & 0x80 ) == 0x80:    #if first bit is 1, direction is left
        car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH, steering)
        car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, GROUND)
        print('steering:' + str(steering) + ' forward')
    else:               #if first bit 0, direction is right
        car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH, GROUND)
        car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, steering + GPIO_TORQUE_CORRECTION) # apply torque correction for right direction
        print('steering: ' + str(steering + GPIO_TORQUE_CORRECTION) + ' backward')


def car_cleanup():
    global car

    car.set_PWM_dutycycle(BCM_PIN_STEERING_HIGH,GROUND)
    car.set_PWM_dutycycle(BCM_PIN_STEERING_LOW, GROUND)
    car.set_PWM_dutycycle(BCM_PIN_SPEED_HIGH,GROUND)
    car.set_PWM_dutycycle(BCM_PIN_SPEED_LOW, GROUND)

    car.stop()
    print('Closed GPIO handler')
    return
#
# car_init()
# car_cleanup()