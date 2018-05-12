import serial
import pynmea2


class GPSSensor:
    SERIAL_BAUDRATE = 9600
    SERIAL_PORT = '/dev/serial0'

    def __init__(self):
        self.ser = serial.Serial(self.SERIAL_PORT, self.SERIAL_BAUDRATE)
        print('Initialized serial interface for GPS Sensor')
        self.shouldIRun = True
        self.gps = None

    def start_measurement(self):
        # self.ser.open()
        while self.shouldIRun:
            data = self.ser.readline().decode('ascii')
            if data[0:6] == '$GPGGA':
                self.gps = pynmea2.parse(data)

    def get_gps(self):
        return self.gps

    def stop(self):
        self.shouldIRun = False
        self.ser.close()
        print('Closed serial interface for GPS Sensor')


# Serial GPS Sensor Test: Expect the coordinates and altitude provided by the sensor
# import _thread
# import time
#
# if __name__ == "__main__":
#     try:
#         dut = GPSSensor()
#         _thread.start_new_thread(dut.start_measurement, ())
#         while True:
#             time.sleep(1)
#             gps = dut.get_gps()
#             print('lat=' + str(gps.latitude) + str(gps.lat_dir) + ' lon=' + str(gps.longitude) + str(
#                 gps.lon_dir) + ' altitude=' + str(gps.altitude) + str(gps.altitude_units))
#
#
#     finally:
#         dut.stop()
