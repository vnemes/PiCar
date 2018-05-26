from django.conf.urls import url

from administration.views import ServiceController, ota

urlpatterns = [
    url(r'^service/$', ServiceController.as_view(), name="speed-ctrl"),
    url(r'^ota/', ota, name="ota-view")
]
