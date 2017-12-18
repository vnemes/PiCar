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

#define SPEED_PIN_HIGH                5
#define SPEED_PIN_LOW                 4


#define STEERING_PIN_HIGH             33
#define STEERING_PIN_LOW              27



class SpeedCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();

      if (value.length() > 0) {
        Serial.print(millis());
        Serial.print("\t\t | SPEED value: ");
        for (int i = 0; i < value.length(); i++){
          Serial.println(int(value[i]));
          if (value[i] & 0x80){
            ledcWrite(1,value[i]<<1);
            ledcWrite(2,GROUND);
          } else {
            ledcWrite(1,GROUND);
            ledcWrite(2,value[i]<<1);
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
          Serial.println(int(value[i]));
          if (value[i] & 0x80){ // CHECK HERE IF *2 CORRECTION IS STILL NEEDED!!!!
            ledcWrite(3,value[i]);
            ledcWrite(4,GROUND);
          } else {
            ledcWrite(3,GROUND);
            ledcWrite(4,value[i]);
          }
        }
      }
    }
};

void setup() {
  Serial.begin(115200);

//  Serial.println("1- Download and install an BLE scanner app in your phone");
//  Serial.println("2- Scan for BLE devices in the app");
//  Serial.println("3- Connect to MyESP32");
//  Serial.println("4- Go to CUSTOM CHARACTERISTIC in CUSTOM SERVICE and write something");
//  Serial.println("5- See the magic =)");
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
  // put your main code here, to run repeatedly:
  delay(2000);
}







/**
 * USE FOR PWM MANIPULATION
 */


//// use first channel of 16 channels (started from zero)
//#define LEDC_CHANNEL_0     0
//
//// use 13 bit precission for LEDC timer
//#define LEDC_TIMER_13_BIT  13
//
//// use 5000 Hz as a LEDC base frequency
//#define LEDC_BASE_FREQ     5000
//
//// fade LED PIN (replace with LED_BUILTIN constant for built-in LED)
//#define LED_PIN            5
//
//int brightness = 0;    // how bright the LED is
//int fadeAmount = 5;    // how many points to fade the LED by
//
//// Arduino like analogWrite
//// value has to be between 0 and valueMax
//void ledcAnalogWrite(uint8_t channel, uint32_t value, uint32_t valueMax = 255) {
//  // calculate duty, 8191 from 2 ^ 13 - 1
//  uint32_t duty = (8191 / valueMax) * min(value, valueMax);
//
//  // write duty to LEDC
//  ledcWrite(channel, duty);
//}
//
//void setup() {
//  // Setup timer and attach timer to a led pin
//  ledcSetup(LEDC_CHANNEL_0, LEDC_BASE_FREQ, LEDC_TIMER_13_BIT);
//  ledcAttachPin(LED_PIN, LEDC_CHANNEL_0);
//}
//
//void loop() {
//  // set the brightness on LEDC channel 0
//  ledcAnalogWrite(LEDC_CHANNEL_0, brightness);

