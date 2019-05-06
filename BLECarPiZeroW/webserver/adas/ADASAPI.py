from flask import Blueprint, request, Response, jsonify

from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
from components.adas.CollisionAvoidanceSystem import CollisionAvoidanceSystem
from components.core.PiRevEn import PiRevEn

adas_api = Blueprint('adas_api', __name__)

acc_service = None
acc = None
colav_service = None
colav = None


@adas_api.route("/adaptivecruise", methods=['POST'])
def service_enable_acc_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    platform = PiRevEn.detect_platform()

    global acc

    if platform == PiRevEn.PIZEROW:
        from components.drivers.ShelbyGT500.HBridgeSpeedDriver import HBridgeSpeedDriver
        acc = AdaptiveCruiseController.get_instance()
        speed_driver = HBridgeSpeedDriver.get_instance()
        acc.enable_disable_driver(enable, speed_driver)

    elif platform == PiRevEn.PI3B_PLUS:
        import rpyc
        global acc_service
        if enable:
            acc_service = rpyc.connect_by_service("AdaptiveCruise")
            acc = acc_service.root
            acc.enable_disable_driver(True)
        else:
            acc.enable_disable_driver(False)
            acc_service.close()
            acc = None
            acc_service = None
    else:
        raise Exception('Cannot initialize adaptive cruise control due to invalid platform!')
    return Response(request.data)


@adas_api.route("/adaptivecruise/status", methods=['GET'])
def acc_status_request():
    status_json = {"enabled": acc.get_status() if acc else False}
    return jsonify(status_json)


@adas_api.route("/collisionavoidance", methods=['POST'])
def service_enable_colav_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    platform = PiRevEn.detect_platform()

    global colav

    if platform == PiRevEn.PIZEROW:
        from components.drivers.ShelbyGT500.HBridgeSpeedDriver import HBridgeSpeedDriver
        colav = CollisionAvoidanceSystem.get_instance()
        speed_driver = HBridgeSpeedDriver.get_instance()
        colav.enable_disable_driver(enable, speed_driver)

    elif platform == PiRevEn.PI3B_PLUS:
        import rpyc
        global colav_service
        if enable:
            colav_service = rpyc.connect_by_service("CollisionAvoidance")
            colav = colav_service.root
            colav.enable_disable_driver(True)
        else:
            colav.enable_disable_driver(False)
            colav.close()
            colav = None
            colav_service = None
    else:
        raise Exception('Cannot initialize collision avoidance due to invalid platform!')
    return Response(request.data)


@adas_api.route("/collisionavoidance/status", methods=['GET'])
def colav_status_request():
    status_json = {"enabled": colav.get_status() if colav else False}
    return jsonify(status_json)
