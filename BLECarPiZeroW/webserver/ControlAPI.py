from flask import Blueprint, request, Response
from components.PiCarController import PiCarController

controller = PiCarController()

control_api = Blueprint('control_api', __name__)


@control_api.route("/speed", methods=['POST'])
def speed_change_request():
    # request.json["direction"] + ' ' + request.json["speed"]
    controller.request_speed(request.json['direction'], request.json['speed'])
    return Response(request.data)


@control_api.route("/steering", methods=['POST'])
def steering_change_request():
    # request.json["direction"] + ' ' + request.json["steering"]
    controller.request_steering(request.json['direction'], request.json['steering'])
    return Response(request.data)


@control_api.route("", methods=['POST'])
def service_enable_request():
    controller.activate_control(request.json['enabled'], request.json['platform'])
    return Response(request.data)

