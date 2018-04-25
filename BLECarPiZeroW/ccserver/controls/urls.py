from django.conf.urls import url
from controls.views import SpeedController, SteeringController

urlpatterns = [
    url(r'^speed/$', SpeedController.as_view(), name="speed-ctrl"),
    url(r'^steering/$', SteeringController.as_view(), name="steering-ctrl"),
]
