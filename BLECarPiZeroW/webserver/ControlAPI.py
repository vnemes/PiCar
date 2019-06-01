from flask import Blueprint, request, Response, jsonify

from components.core.PiRevEn import PiRevEn
from components.core.PlatformEn import PlatformEn

from components.core.WatchDog import WatchDog

__speed_service = None
__steer_service = None
speed_driver = None
steer_driver = None
controller = None

control_api = Blueprint('control_api', __name__)


@control_api.route("/speed", methods=['POST'])
def speed_change_request():
    if controller:
        controller.request_speed(request.json['direction'], request.json['speed'])
    else:
        speed_driver.set_speed(request.json['direction'], request.json['speed'])
    return Response(request.data)


@control_api.route("/steering", methods=['POST'])
def steering_change_request():
    if controller:
        controller.request_steering(request.json['direction'], request.json['steering'])
    else:
        steer_driver.set_steering(request.json['direction'], request.json['steering'])
    return Response(request.data)


@control_api.route("/speed/limit", methods=['POST'])
def speed_limit_change_request():
    if controller:
        controller.set_speed_limit(request.json['limit'])
    else:
        speed_driver.change_speed_limit(request.json['limit'])
    return Response(request.data)


@control_api.route("/speed/status", methods=['GET'])
def speed_status_request():
    status_json = {"enabled": controller.get_speed_status() if controller
                    else speed_driver.get_status() if speed_driver else False}
    return jsonify(status_json)


@control_api.route("/steering/status", methods=['GET'])
def steering_status_request():
    status_json = {"enabled": controller.get_steering_status() if controller
                    else steer_driver.get_status() if steer_driver else False}
    return jsonify(status_json)


@control_api.route("", methods=['POST'])
def service_enable_request():
    enable = True if request.json['enabled'].lower() == "true" else False

    WatchDog.get_instance().enable_disable_driver(enable)
    global controller
    platform = PiRevEn.detect_platform()

    if PlatformEn[request.json['platform']] == PlatformEn.SHELBYGT500:
        from components.core.PiCarController import PiCarController
        controller = PiCarController.get_instance()
        controller.activate_control(enable, PlatformEn.SHELBYGT500)
        return Response(request.data)

    if platform == PiRevEn.PI3B_PLUS:
        global __speed_service, __steer_service, speed_driver, steer_driver
        controller = None

        import rpyc
        if enable and not (speed_driver or steer_driver):
            __speed_service = rpyc.connect_by_service("SpeedDriver")
            __steer_service = rpyc.connect_by_service("SteeringDriver")
            speed_driver = __speed_service.root
            steer_driver = __steer_service.root
            speed_driver.enable_disable_driver(True)
            steer_driver.enable_disable_driver(True)
        else:
            speed_driver.enable_disable_driver(False)
            steer_driver.enable_disable_driver(False)
            __speed_service.close()
            __steer_service.close()
            speed_driver, steer_driver = None, None
            __speed_service, __steer_service = None, None
        return Response(request.data)
    else:
        return Response(status=409)


