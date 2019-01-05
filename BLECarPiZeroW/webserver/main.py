from flask import Flask, request, Response
from drivers import PiCarController, UltrasonicSensor
import atexit

app = Flask(__name__)

piCarController = None
ultrasonicSensor = UltrasonicSensor.UltrasonicSensor()


@app.route('/control', methods=['POST'])
def enable_disable_controls():
    requested_value = request.args.get('enabled')
    global piCarController
    if requested_value == 'true' and piCarController is None:
        piCarController = PiCarController.PiCarController()
    elif requested_value == 'false' and piCarController is not None:
        piCarController.stop()
        piCarController = None
    else:
        return Response(status=400)
    return Response(status=200)


@app.route('/control/speed', methods=['POST'])
def speed():
    global piCarController
    if piCarController is not None:
        piCarController.set_speed(request.json["direction"], request.json["speed"])
        return Response(status=200)
    else:
        return Response(status=400)


@app.route('/control/steering', methods=['POST'])
def steering():
    global piCarController
    if piCarController is not None:
        piCarController.set_steering(request.json["direction"], request.json["steering"])
        return Response(status=200)
    else:
        return Response(status=400)


# defining function to run on shutdown
def close_running_threads():
    print("Shutdown requested, ready to finish")
    global piCarController, ultrasonicSensor
    if piCarController is not None:
        piCarController.stop()

    if ultrasonicSensor is not None:
        ultrasonicSensor.stop()


if __name__ == '__main__':
    # Register to be called on exit
    atexit.register(close_running_threads)
    app.run(debug=True, host='0.0.0.0')
