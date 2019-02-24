from flask import Blueprint, request, Response
from components.PiCarController import PiCarController
from components.drivers.PlatformEn import PlatformEn

controller = PiCarController.get_instance()

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
    enable = True if request.json['enabled'].lower() == "true" else False
    platform = PlatformEn[request.json['platform']]
    controller.activate_control(enable, platform)
    return Response(request.data)


@control_api.route("/adaptivecruise", methods=['POST'])
def service_enable_acc_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    controller.activate_cruise_control(enable)
    return Response(request.data)

