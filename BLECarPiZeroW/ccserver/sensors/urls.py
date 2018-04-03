from django.conf.urls import url
from sensors.views import DemoSensor

urlpatterns = [
    url(r'^demo/$', DemoSensor.as_view(), name="sensors-demo"),
]
