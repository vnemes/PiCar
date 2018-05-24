import subprocess

from django.shortcuts import render

# Create your views here.
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from shared.permissions import PublicEndpoint


class ServiceController(APIView):
    """
    The ServiceController controls the systemd services.
    """
    permission_classes = (PublicEndpoint,)

    def post(self, request):
        try:
            service = request.data["service"]
            cmd = request.data["cmd"]

            whitelisted_services = ["gps", "ultrasonic-sensor", "ota", "picar-controller", "picamera"]
            if service not in whitelisted_services:
                return Response("Invalid service!", status=status.HTTP_400_BAD_REQUEST)

            if cmd in ["start", "stop", "restart", "status"]:
                output = subprocess.check_output("systemctl {} {}".format(cmd, service),
                                                 shell=True)
                return Response(output, status=status.HTTP_200_OK)

            return Response("Invalid command!", status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response(str(e), status=status.HTTP_400_BAD_REQUEST)
