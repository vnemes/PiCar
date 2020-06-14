from enum import Enum

PI_3b_plus_string = 'Raspberry Pi 3 Model B Plus'
Pi_zero_w_string = 'Raspberry Pi Zero W'


class PiRevEn(Enum):

    PI3B_PLUS = 0
    PIZEROW = 1

    @staticmethod
    def detect_platform():
        platform_file = open('/sys/firmware/devicetree/base/model', "r")
        for line in platform_file:
            if PI_3b_plus_string in line:
                return PiRevEn.PI3B_PLUS
            elif Pi_zero_w_string in line:
                return PiRevEn.PIZEROW
