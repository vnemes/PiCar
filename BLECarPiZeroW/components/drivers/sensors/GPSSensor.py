import serial
import pynmea2
import threading
from ..IDriver import IDriver


class GPSSensor(IDriver):
    __instance = None
    SERIAL_BAUDRATE = 9600
    SERIAL_PORT = '/dev/serial0'
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if GPSSensor.__instance is None:
            GPSSensor()
        return GPSSensor.__instance

    def __init__(self):
        if GPSSensor.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            GPSSensor.__instance = self
            self.data_thread = None
            self.latest_gps_value = None
            self.ser = None
        return

    def get_data(self):
        return self.latest_gps_value

    def __collect_data(self):
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            data = self.ser.readline().decode('ascii')
            if data[0:6] == '$GPGGA':
                self.latest_gps_value = pynmea2.parse(data)
        return

    def start(self):
        if not self.data_thread or not self.data_thread.is_alive():
            try:
                self.ser = serial.Serial(self.SERIAL_PORT, self.SERIAL_BAUDRATE)
                print('Initialized serial interface for GPSSensor')
                self.latest_gps_value = None
                # self.ser.open()
                self.data_thread = threading.Thread(target=self.__collect_data)
                self.data_thread.daemon = True
                self.data_thread.start()
            except serial.serialutil.SerialException as ex:
                print('Error connecting to GPS: ' + str(ex))
        return

    def stop(self):
        if self.data_thread and self.data_thread.is_alive():
            self.data_thread.THREAD_RUN_REQUESTED = False
            self.data_thread.join()
            self.ser.close()
            print('Closed serial interface for GPS Sensor')
        return
