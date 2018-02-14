import socket
import time
import picamera


print('starting camera')

camera = picamera.PiCamera()
camera.resolution = '720p'
camera.framerate = 60

server_socket = socket.socket()
server_socket.bind(('0.0.0.0', 8000))
server_socket.listen(0)

# Accept a single connection and make a file-like object out of it
connection = server_socket.accept()[0].makefile('wb')
try:
    print('attempting stream start')
    camera.start_recording(connection, format='h264')
    camera.wait_recording(60)
    camera.stop_recording()
    camera.close()
finally:
    connection.close()
    server_socket.shutdown(1)
    server_socket.close()
    print('successfully closed stream')