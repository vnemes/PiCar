from flask import Blueprint, request, Response

control_api = Blueprint('control_api', __name__)


@control_api.route("/speed", methods=['POST'])
def speed_change_request():
    # request.json["direction"] + ' ' + request.json["speed"]
    return Response(request.data)


@control_api.route("/steering", methods=['POST'])
def steering_change_request():
    # request.json["direction"] + ' ' + request.json["steering"]
    return Response(request.data)
