import subprocess

from django import forms
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse
from django.shortcuts import render

# Create your views here.
from django.views.decorators.csrf import csrf_exempt
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


class OtaForm(forms.Form):
    CHOICES = (
        ('/usr/bin/picamera_start', 'PiCamera',),
        ('/home/vendetta/PiCar/dbusObjects/UltrasonicSensorDBUS.py', 'UltrasonicSensorDBUS',),
        ('/home/vendetta/PiCar/dbusObjects/PiCarControllerDBUS.py', 'PiCarControllerDBUS',),
        ('/home/vendetta/PiCar/dbusObjects/GPSSensorDBUS.py', 'GPSSensorDBUS'),
        ('/home/vendetta/PiCar/dbusObjects/drivers/GPSSensor.py', 'GPSSensor.py'),
        ('/home/vendetta/PiCar/dbusObjects/drivers/PiCarController.py', 'PiCarController.py'),
        ('/home/vendetta/PiCar/dbusObjects/drivers/UltrasonicSensor.py', 'UltrasonicSensor.py'),
        # ('/Users/denis/PycharmProjects/blecar/BLECar/BLECarPiZeroW/ccserver/test.zip', 'Test')
    )

    module = forms.ChoiceField(choices=CHOICES)
    data_file = forms.FileField()


class ServiceForm(forms.Form):
    CHOICES = (
        ('gps', 'GPS Service',),
        ('ultrasonic-sensor', 'UltrasonicSensor Service',),
        ('picar-controller', 'PiCarController Service'),
        ('picamera', 'PiCamera Service'),
    )

    CHOICES2 = {
        ('start', 'Start'),
        ('stop', 'Stop'),
        ('restart', 'Restart'),
        ('status', 'Status')
    }

    service = forms.ChoiceField(choices=CHOICES)
    cmd = forms.ChoiceField(choices=CHOICES2)


# horrible code just for demoing purposes.
@login_required
def ota(request):
    sform = ServiceForm()
    if request.method == 'POST':
        form = OtaForm(request.POST, request.FILES)
        if form.is_valid():
            module = form.data['module']
            file = request.FILES['data_file']

            try:
                fp = open(module, "wb")
                fp.write(file.read())
            except Exception as e:
                return HttpResponse(str(e))

            return HttpResponse("Done!")
    else:
        form = OtaForm()
    return render(request, 'over_the_air.html', {'form': form, 'service_form': sform})
