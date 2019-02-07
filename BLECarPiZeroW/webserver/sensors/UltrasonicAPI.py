from flask import Blueprint, request, Response

ultrasonic_api = Blueprint('ultrasonic_api', __name__)


@ultrasonic_api.route("/ultrasonic", methods=['GET'])
def ultrasonic_value_request():
    return Response('ultrasonic data')
