from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from shared.permissions import PublicEndpoint

import dbus
bus = dbus.SessionBus()

# Create your views here.
class SpeedController(APIView):
    """
    The SpeedController controls the speed of the car.
    """
    permission_classes = (PublicEndpoint,)

    def post(self, request):
        try:
            object = bus.get_object("picar.control.speedsteering", "/picar/control/speedsteering")
            interface = dbus.Interface(object, "picar.control.speedsteering")


            interface.setSpeed(int(request.data["direction"]), int(request.data["speed"]))
            return Response(None, status=status.HTTP_200_OK)
        except Exception:
            return Response(None, status=status.HTTP_400_BAD_REQUEST)

class SteeringController(APIView):
    """
    The SteeringController controls the steering of the car.
    """
    permission_classes = (PublicEndpoint,)

    def post(self, request):
        try:
            object = bus.get_object("picar.control.speedsteering", "/picar/control/speedsteering")
            interface = dbus.Interface(object, "picar.control.speedsteering")

            interface.setSteering(int(request.data["direction"]), int(request.data["steering"]))
            return Response(None, status=status.HTTP_200_OK)
        except Exception:
            return Response(None, status=status.HTTP_400_BAD_REQUEST)