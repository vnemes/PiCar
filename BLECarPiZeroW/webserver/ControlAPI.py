from flask import Blueprint, request, Response, jsonify
from components.core.PlatformEn import PlatformEn
import rpyc

__control_service = None
controller = None

control_api = Blueprint('control_api', __name__)


@control_api.route("/speed", methods=['POST'])
def speed_change_request():
    controller.request_speed(request.json['direction'], request.json['speed'])
    return Response(request.data)


@control_api.route("/steering", methods=['POST'])
def steering_change_request():
    controller.request_steering(request.json['direction'], request.json['steering'])
    return Response(request.data)


@control_api.route("/speed/limit", methods=['POST'])
def speed_limit_change_request():
    controller.set_speed_limit(request.json['limit'])
    return Response(request.data)


@control_api.route("/speed/status", methods=['GET'])
def speed_status_request():
    status_json = {"enabled": controller.get_speed_status() if controller else False}
    return jsonify(status_json)


@control_api.route("/steering/status", methods=['GET'])
def steering_status_request():
    status_json = {"enabled": controller.get_steering_status() if controller else False}
    return jsonify(status_json)


@control_api.route("", methods=['POST'])
def service_enable_request():
    enable = True if request.json['enabled'].lower() == "true" else False

    global controller

    if PlatformEn[request.json['platform']] == PlatformEn.SHELBYGT500:
        from components.core.PiCarController import PiCarController
        controller = PiCarController.get_instance()
        controller.activate_control(enable, PlatformEn.SHELBYGT500)
        return Response(request.data)

    global __control_service

    if enable and not controller:
        __control_service = rpyc.connect_by_service("Controller")
        controller = __control_service.root
        controller.enable_disable_driver(True)
    else:
        controller.enable_disable_driver(False)
        __control_service.close()
        controller = None
        __control_service = None

    return Response(request.data)


