from flask import Blueprint, request, Response
from components.core.PlatformEn import PlatformEn
import rpyc

__control_service = None
controller = None

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

    global controller

    if PlatformEn[request.json['platform']] == PlatformEn.SHELBYGT500_no_service:
        from components.core.PiCarController import PiCarController
        controller = PiCarController.get_instance()
        controller.activate_control(enable, PlatformEn.SHELBYGT500)
        return Response(request.data)

    global __control_service

    if enable:
        __control_service = rpyc.connect_by_service("Controller")
        controller = __control_service.root
        controller.enable_disable_driver(True)
    else:
        controller.enable_disable_driver(False)
        __control_service.close()

    return Response(request.data)


@control_api.route("/adaptivecruise", methods=['POST'])
def service_enable_acc_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    controller.activate_cruise_control(enable)
    return Response(request.data)

