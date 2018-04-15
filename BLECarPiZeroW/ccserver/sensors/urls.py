from django.conf.urls import url
from sensors.views import DemoSensor, UltrasonicSensor, SpeedSensor

urlpatterns = [
    url(r'^speed/$', SpeedSensor.as_view(), name="sensors-speed"),
    url(r'^ultrasonic/$', UltrasonicSensor.as_view(), name="sensors-ultrasonic"),
    url(r'^demo/$', DemoSensor.as_view(), name="sensors-demo"),
]
