from flask import Blueprint, request, Response, jsonify
from components.drivers.sensors.UltrasonicSensor import UltrasonicSensor

ultrasonic = UltrasonicSensor.get_instance()

ultrasonic_api = Blueprint('ultrasonic_api', __name__)


@ultrasonic_api.route("/ultrasonic", methods=['GET'])
def ultrasonic_value_request():
    ultrasonic_data = ultrasonic.get_data()
    if ultrasonic_data:
        ultrasonic_json = {"sensor_name": "ultrasonic_sensor",
                           "distance": ultrasonic_data}
        return jsonify(ultrasonic_json)
    else:
        return Response(status=204)


@ultrasonic_api.route("/ultrasonic", methods=['POST'])
def service_enable_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    ultrasonic.enable_disable_driver(enable)
    return Response(request.data)
