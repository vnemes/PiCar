import threading

import serial
import time

from components.drivers.AbstractComponent import AbstractComponent


class JeVoisDriver(AbstractComponent):
    __instance = None
    THREAD_RUN_REQUESTED = 'THREAD_RUN_REQUESTED'

    @staticmethod
    def get_instance():
        """ Static access method. """
        if JeVoisDriver.__instance is None:
            JeVoisDriver()
        return JeVoisDriver.__instance

    def __init__(self):
        if JeVoisDriver.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            JeVoisDriver.__instance = self
            self.ser = None
            self.data_thread = None
            self.vanishing_point = None
            self.lock = threading.Lock()

    def get_data(self):
        if not self.started:
            self.enable_disable_driver(True)
        with self.lock:
            return self.vanishing_point

    def start(self):
        if not self.started:
            self.ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200, timeout=0.2)
            self.__send_command(b'streamoff\n')
            self.__send_command(b'setpar serlog None\n')
            self.__send_command(b'setpar serout All\n')
            self.__send_command(b'setpar horizon 120\n')
            # self.__send_command(b'setpar serstyle Normal\n') # disabled: params normalized -1k,+1k
            self.__send_command(b'setmapping 8\n')
            self.__send_command(b'streamon\n')
            print('Initialized Serial for Jevois Camera')
            self.data_thread = threading.Thread(target=self.__collect_data)
            self.data_thread.daemon = True
            self.data_thread.start()

    def stop(self):
        if self.data_thread and self.data_thread.is_alive():
            self.data_thread.THREAD_RUN_REQUESTED = False
            self.data_thread.join()
            self.__send_command(b'streamoff\n')
            self.ser.close()
            print('Closed Serial for Jevois Camera')
        return

    def __send_command(self, cmd):
        self.ser.write(cmd)
        print(self.ser.readline().decode('ascii'))

    def __collect_data(self):
        t = threading.current_thread()
        while getattr(t, self.THREAD_RUN_REQUESTED, True):
            line = self.ser.readline().decode('ascii')
            words = line.split()
            if len(words) > 1:
                if words[0] == 'T1':
                    with self.lock:
                        self.vanishing_point = int(words[1])
                    # print('vanishing point: ' + str(self.vanishing_point))
        return


# if __name__ == '__main__':
#     try:
#         driver = JeVoisDriver.get_instance()
#         driver.enable_disable_driver(True)
#         while(True):
#             print(str(driver.get_data()))
#     finally:
#         driver.enable_disable_driver(False)
