from django.conf.urls import url
from sensors.views import DemoSensor, UltrasonicSensor, SpeedSensor

urlpatterns = [
    url(r'^speed/$', SpeedSensor.as_view(), name="speed-sensor"),
    url(r'^ultrasonic/$', UltrasonicSensor.as_view(), name="ultrasonic-sensor"),
    url(r'^demo/$', DemoSensor.as_view(), name="demo-sensor"),
]
