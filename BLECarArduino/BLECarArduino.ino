/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleWrite.cpp
    Ported to Arduino ESP32 by Evandro Copercini
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define BLE_DEVICE_NAME              "BLECar"

#define MOVEMENT_SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define STEERING_CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define SPEED_CHARACTERISTIC_UUID    "2eabb1e1-ae0f-4eb8-bfdc-f564ad55f359"

#define GROUND                        0x00
#define MINIMUM_TORQUE_CORRECTION     0x80

#define SPEED_PIN_HIGH                19
#define SPEED_PIN_LOW                 18


#define STEERING_PIN_HIGH             17
#define STEERING_PIN_LOW              16



class SpeedCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();

      if (value.length() > 0) {
        Serial.print(millis());
        Serial.print("\t\t | SPEED value: ");
        for (int i = 0; i < value.length(); i++){
          if (value[i] & 0x80){
            if (value[i] & 0x7F) // do not apply correction for 0
              ledcWrite(1,(value[i] & (~0x80)) + MINIMUM_TORQUE_CORRECTION);
            else ledcWrite(1,GROUND);
            ledcWrite(2,GROUND);
            Serial.println(int((value[i] & (~0x80)) + MINIMUM_TORQUE_CORRECTION));
          } else {
            if (value[i] & 0x7F) // do not apply correction for 0
              ledcWrite(2,value[i] + MINIMUM_TORQUE_CORRECTION);
            else ledcWrite(2,GROUND); 
            ledcWrite(1,GROUND);
            Serial.println(-int(value[i] + MINIMUM_TORQUE_CORRECTION));
          }
        }
      }
    }
};

class SteeringCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
    
      if (value.length() > 0) {
        Serial.print(millis());
        Serial.print("\t\t | STEERING value: ");
        for (int i = 0; i < value.length(); i++){
          if (value[i] & 0x80){ 
              //TODO -  Check if *2 correction still needs to be applied 
              // (torque of the DC Motor responsible with steering)
            ledcWrite(3,(value[i] & (~0x80))<<1);
            ledcWrite(4,GROUND);
            Serial.println(int((value[i] & (~0x80))<<1));
          } else {
            ledcWrite(3,GROUND);
            ledcWrite(4,(value[i] & (~0x80))<<1);
            Serial.println(-int(value[i]<<1));
          }
        }
      }
    }
};

void setup() {
  Serial.begin(115200);

  Serial.print(millis());
  Serial.println("\t\t | BLECar started");

  // Initialize DC Motor pins for PWM
  ledcAttachPin(SPEED_PIN_HIGH,1);
  ledcAttachPin(SPEED_PIN_LOW,2);
  ledcAttachPin(STEERING_PIN_HIGH,3);
  ledcAttachPin(STEERING_PIN_LOW,4);
  // Initialize channels 
  // channels 0-15, resolution 1-16 bits, freq limits depend on resolution
  // ledcSetup(uint8_t channel, uint32_t freq, uint8_t resolution_bits);
  ledcSetup(1, 22000, 8); // 22 kHz PWM, 8-bit resolution
  ledcSetup(2, 22000, 8);
  ledcSetup(3, 22000, 8); // 22 kHz PWM, 8-bit resolution
  ledcSetup(4, 22000, 8);


  BLEDevice::init(BLE_DEVICE_NAME);
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(MOVEMENT_SERVICE_UUID);

  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         STEERING_CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE
                                       );

  pCharacteristic->setCallbacks(new SteeringCallback());

  pCharacteristic->setValue("\0");

  BLECharacteristic *sCharacteristic = pService->createCharacteristic(
                                         SPEED_CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE
                                       );

  sCharacteristic->setCallbacks(new SpeedCallback());

  sCharacteristic->setValue("\0");
  
  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();
}

void loop() {
 
  delay(2000);
}
