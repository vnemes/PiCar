from django.conf.urls import url

from administration.views import ServiceController

urlpatterns = [
    url(r'^service/$', ServiceController.as_view(), name="speed-ctrl"),
]
