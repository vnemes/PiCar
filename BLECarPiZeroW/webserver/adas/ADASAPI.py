import rpyc
from flask import Blueprint, request, Response, jsonify

from components.adas.AdaptiveCruiseController import AdaptiveCruiseController
from components.adas.CollisionAvoidanceSystem import CollisionAvoidanceSystem
from components.core.PiCarController import PiCarController
from components.core.PiRevEn import PiRevEn

adas_api = Blueprint('adas_api', __name__)

acc_service = None
acc = None


@adas_api.route("/adaptivecruise", methods=['POST'])
def service_enable_acc_request():
    enable = True if request.json['enabled'].lower() == "true" else False
    platform = PiRevEn.detect_platform()

    global acc

    if platform == PiRevEn.PIZEROW:
        acc = AdaptiveCruiseController.get_instance()
        controller = PiCarController.get_instance()
        acc.enable_disable_driver(enable, controller)

    elif platform == PiRevEn.PI3B_PLUS:

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
def service_enable_col_av_request():
    enable = True if request.json['enabled'].lower() == "true" else False

    collision_avoidance = CollisionAvoidanceSystem.get_instance()
    collision_avoidance.enable_disable_driver(enable)
    return Response(request.data)
