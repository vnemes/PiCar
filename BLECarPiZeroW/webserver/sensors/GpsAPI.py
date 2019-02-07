from flask import Blueprint, Response

gps_api = Blueprint('gps_api', __name__)


@gps_api.route("/gps", methods=['GET'])
def gps_value_request():
    return Response('gps data')
