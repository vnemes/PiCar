from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from shared.permissions import PublicEndpoint


# Create your views here.
class SpeedController(APIView):
    """
    The SpeedController controls the speed of the car.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        return Response(None, status=status.HTTP_200_OK)

    def post(self, request):
        return Response(None, status=status.HTTP_200_OK)


class SteeringController(APIView):
    """
    The SteeringController controls the steering of the car.
    """
    permission_classes = (PublicEndpoint,)

    def get(self, request):
        return Response(None, status=status.HTTP_200_OK)

    def post(self, request):
        return Response(None, status=status.HTTP_200_OK)
