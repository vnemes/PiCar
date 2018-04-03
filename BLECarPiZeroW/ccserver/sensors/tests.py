from django.urls import reverse
from rest_framework.test import APITestCase
from rest_framework import status


class SensorsTest(APITestCase):
    def setUp(self):
        # URL for demo.
        self.sensor_demo_url = reverse('sensors-demo')

    def test_demo_sensor_get(self):
        """
        Ensure that DemoSensor returns a valid response on GET.
        """
        response = self.client.get(self.sensor_demo_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data["demo"], True)
