from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from shared.permissions import PublicEndpoint

import dbus
bus = dbus.SessionBus()

class SpeedSensor(APIView):
    """
    The speed sensor controls the car's speed.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        try:
            object = bus.get_object("picar.sensor.speed", "/picar/sensor/speed")
            interface = dbus.Interface(object, "picar.sensor.speed")
            json = {
                "sensor_name": "speed_sensor",
                "speed": interface.getSpeed(),
                "direction": interface.getDirection(),
            }
            return Response(json, status=status.HTTP_200_OK)
        except Exception:
            return Response({"message": "The module bus is offline!"},
                            status=status.HTTP_503_SERVICE_UNAVAILABLE)


class UltrasonicSensor(APIView):
    """
    Ultrasonic sensor view gets data from the ultrasonic sensor.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        try:
            object = bus.get_object("picar.sensor.ultrasonic", "/picar/sensor/ultrasonic")
            interface = dbus.Interface(object, "picar.sensor.ultrasonic")
            json = {
                "sensor_name": "ultrasonic_sensor",
                "distance": interface.getDistance(),
            }
            return Response(json, status=status.HTTP_200_OK)
        except Exception:
            return Response({"message": "The module bus is offline!", "distance": -1},
                            status=status.HTTP_503_SERVICE_UNAVAILABLE)


class DemoSensor(APIView):
    """
    Just a demo sensor, demonstrating that everything is working.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        json = {
            "sensor_name": "demo_sensor",
            "x": 100,
            "y": 100,
            "demo": True
        }
        return Response(json, status=status.HTTP_200_OK)