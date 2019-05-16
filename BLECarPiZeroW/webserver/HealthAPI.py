from flask import Blueprint, Response, jsonify
import os

from components.core.WatchDog import WatchDog

health_api = Blueprint('health_api', __name__)


@health_api.route("", methods=['GET'])
def health_check():
    WatchDog.get_instance().reset()
    json = {"status": "ok"}
    return jsonify(json)


@health_api.route("/temperature", methods=['GET'])
def temperature_request():
    temp_json = {"sensor_name": "temperature_sensor",
                 "temperature": measure_temperature()}
    return jsonify(temp_json)


def measure_temperature():
    temp = os.popen("vcgencmd measure_temp").readline()
    return float(temp.replace("temp=", "").replace("'C", ""))
