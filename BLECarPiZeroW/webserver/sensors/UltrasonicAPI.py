from flask import Blueprint, request, Response, jsonify

from components.core.PiRevEn import PiRevEn
from components.drivers.sensors.UltrasonicSensor import UltrasonicSensor

ultrasonic_api = Blueprint('ultrasonic_api', __name__)

ultrasonic_service = None
ultrasonic = None


@ultrasonic_api.route("/ultrasonic", methods=['GET'])
def ultrasonic_value_request():
    ultrasonic_data = ultrasonic.get_data()
    if ultrasonic_data:
        ultrasonic_json = {"sensor_name": "ultrasonic_sensor",
                           "distance": ultrasonic_data}
        return jsonify(ultrasonic_json)
    else:
        return Response(status=204)


@ultrasonic_api.route("/ultrasonic/status", methods=['GET'])
def ultrasonic_status_request():
    status_json = {"enabled": ultrasonic.get_status() if ultrasonic else False}
    return jsonify(status_json)


@ultrasonic_api.route("/ultrasonic", methods=['POST'])
def service_enable_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    platform = PiRevEn.detect_platform()
    global ultrasonic

    if platform == PiRevEn.PIZEROW:
        ultrasonic = UltrasonicSensor.get_instance()
        ultrasonic.enable_disable_driver(enable)

    elif platform == PiRevEn.PI3B_PLUS:
        global ultrasonic_service

        if enable:
            import rpyc
            ultrasonic_service = rpyc.connect_by_service("UltrasonicSensor")
            ultrasonic = ultrasonic_service.root
            ultrasonic.enable_disable_driver(True)
        elif ultrasonic:
            ultrasonic.enable_disable_driver(False)
            ultrasonic_service.close()
            ultrasonic = None
            ultrasonic_service = None
    else:
        raise Exception('Cannot initialize Ultrasonic Sensor due to invalid platform!')
    return Response(request.data)

