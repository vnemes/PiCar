from django.conf.urls import url
from sensors.views import DemoSensor, UltrasonicSensor, \
    SpeedSensor, CameraSensor, GPSSensor

urlpatterns = [
    url(r'^speed/$', SpeedSensor.as_view(), name="speed-sensor"),
    url(r'^camera/$', CameraSensor.as_view(), name="camera-sensor"),
    url(r'^gps/$', GPSSensor.as_view(), name="gps-sensor"),
    url(r'^ultrasonic/$', UltrasonicSensor.as_view(), name="ultrasonic-sensor"),
    url(r'^demo/$', DemoSensor.as_view(), name="demo-sensor"),
]
