import atexit
from flask import Flask


from components.drivers.PlatformEn import PlatformEn
from webserver.ControlAPI import control_api
from webserver.HealthAPI import health_api
from webserver.sensors.UltrasonicAPI import ultrasonic_api
from webserver.sensors.GpsAPI import gps_api

app = Flask(__name__)

app.register_blueprint(control_api, url_prefix='/control')
app.register_blueprint(health_api, url_prefix='/health')
app.register_blueprint(ultrasonic_api, url_prefix='/sensor')
app.register_blueprint(gps_api, url_prefix='/sensor')


def cleanup():
    import RPi
    RPi.GPIO.cleanup()

if __name__ == '__main__':
    # Register to be called on exit

    atexit.register(cleanup)
    app.run(debug=True, host='0.0.0.0')
