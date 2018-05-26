from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from shared.permissions import PublicEndpoint
import os
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


class CameraSensor(APIView):
    """
    The camera sensor controls the cameras' status
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        return Response({"message": "Not implemented"}, status=status.HTTP_200_OK)

    def post(self, reqest):
        try:
            cmd = reqest.data["status"]
            if cmd == "restart":
                os.system("systemctl restart picamera")
            if cmd == "start":
                os.system("systemctl start picamera")
            if cmd == "stop":
                os.system("systemctl stop picamera")

            return Response({"message": "Done", "command": cmd}, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"message": str(e)}, status=status.HTTP_400_BAD_REQUEST)


class GPSSensor(APIView):
    """
    The gps sensor view controls the GPS module.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        object = bus.get_object("picar.sensor.gps", "/picar/sensor/gps")
        interface = dbus.Interface(object, "picar.sensor.gps")

        data = interface.getCoords()

        return Response(data, status=status.HTTP_200_OK)

    def post(self, reqest):
        try:
            cmd = reqest.data["cmd"]
            if cmd == "restart":
                os.system("systemctl restart gps")
            if cmd == "start":
                os.system("systemctl start gps")
            if cmd == "stop":
                os.system("systemctl stop gps")

            return Response({"message": "Done", "command": cmd}, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"message": str(e)}, status=status.HTTP_400_BAD_REQUEST)


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
