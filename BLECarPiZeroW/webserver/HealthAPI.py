from flask import Blueprint, Response
import os

health_api = Blueprint('health_api', __name__)


@health_api.route("/", methods=['GET'])
def health_check():
    return Response(status=200)


@health_api.route("/temperature", methods=['GET'])
def temperature_request():
    return Response(measure_temperature())


def measure_temperature():
    temp = os.popen("vcgencmd measure_temp").readline()
    return temp.replace("temp=", "")
