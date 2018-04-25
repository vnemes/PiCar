from django.urls import reverse
from rest_framework.test import APITestCase
from rest_framework import status


class ControlsTest(APITestCase):
    def setUp(self):
        # URL for demo.
        self.speed_ctrl = reverse('speed-ctrl')

    def test_speed_ctrl_get(self):
        response = self.client.get(self.speed_ctrl)
        self.assertEqual(response.status_code, status.HTTP_200_OK)