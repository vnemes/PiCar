/*
    BLE Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleWrite.cpp
    OV7670 camera driver & esp32 integration from
    https://github.com/bitluni/ESP32CameraI2S and https://github.com/igrr/esp32-cam-demo
*/

/**
 Compiler switches for enabling/disabling functionalities
**/
//#define BLUETOOTH_CONTROL_ON // bluetooth speed & steering control
#define WIFI_CONTROL_ON // wifi speed & steering control
//#define WIFI_CAMERA_ON // wifi webserver and camera functionality
#define DEBUG_MODE // logging enabled
/**
  End compiler switches
**/

#ifdef BLUETOOTH_CONTROL_ON
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#endif

#ifdef WIFI_CAMERA_ON
#include "OV7670.h"
#include <WiFi.h>
#include <WiFiMulti.h>
#include <WiFiClient.h>
#include "BMP.h"
#endif

#ifdef BLUETOOTH_CONTROL_ON
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


#define STEERING_PIN_HIGH             26 //17
#define STEERING_PIN_LOW              25 //16




class SpeedCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();

     if (value.length() > 0) {
        #ifdef DEBUG_MODE
        Serial.print(millis());
        Serial.print("\t\t | SPEED value: ");
        #endif
        for (int i = 0; i < value.length(); i++){
          if (value[i] & 0x80){
            if (value[i] & 0x7F) // do not apply correction for 0
              ledcWrite(1,(value[i] & (~0x80)) + MINIMUM_TORQUE_CORRECTION);
            else ledcWrite(1,GROUND);
            ledcWrite(2,GROUND);
            #ifdef DEBUG_MODE
            Serial.println(int((value[i] & (~0x80)) + MINIMUM_TORQUE_CORRECTION));
            #endif
          } else {
            if (value[i] & 0x7F) // do not apply correction for 0
              ledcWrite(2,value[i] + MINIMUM_TORQUE_CORRECTION);
            else ledcWrite(2,GROUND); 
            ledcWrite(1,GROUND);
            #ifdef DEBUG_MODE
            Serial.println(-int(value[i] + MINIMUM_TORQUE_CORRECTION));
            #endif
          }
        }
      }
    }
};

class SteeringCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
    
      if (value.length() > 0) {
            #ifdef DEBUG_MODE
        Serial.print(millis());
        Serial.print("\t\t | STEERING value: ");
            #endif
        for (int i = 0; i < value.length(); i++){
          if (value[i] & 0x80){ 
              //TODO -  Check if *2 correction still needs to be applied 
              // (torque of the DC Motor responsible with steering)
            ledcWrite(3,(value[i] & (~0x80))<<1);
            ledcWrite(4,GROUND);
            #ifdef DEBUG_MODE
            Serial.println(int((value[i] & (~0x80))<<1));
            #endif
          } else {
            ledcWrite(3,GROUND);
            ledcWrite(4,(value[i] & (~0x80))<<1);
            #ifdef DEBUG_MODE
            Serial.println(-int(value[i]<<1));
            #endif
          }
        }
      }
    }
};
#endif

#ifdef WIFI_CAMERA_ON

const int SIOD = 21; //SDA
const int SIOC = 22; //SCL

const int VSYNC = 34;
const int HREF = 35;

const int XCLK = 32;
const int PCLK = 33;

const int D0 = 27;
const int D1 = 17;
const int D2 = 16;
const int D3 = 15;
const int D4 = 14;
const int D5 = 13;
const int D6 = 12;
const int D7 = 4;

const int TFT_DC = 2;
const int TFT_CS = 5;
//DIN <- MOSI 23
//CLK <- SCK 18

#define ssid1        "Vendetta OnePlus 5"
#define password1    "INeverUseThisPassword"

OV7670 *camera;

WiFiMulti wifiMulti;
WiFiServer server(80);

unsigned char bmpHeader[BMP::headerSize];

void serve()
{
  WiFiClient client = server.available();
  if (client) 
  {
    #ifdef DEBUG_MODE
    Serial.println("New Client.");
    #endif
    String currentLine = "";
    while (client.connected()) 
    {
      if (client.available()) 
      {
        char c = client.read();
        #ifdef DEBUG_MODE
        Serial.write(c);
        #endif
        if (c == '\n') 
        {
          if (currentLine.length() == 0) 
          {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();
            client.print(
              "<style>body{margin: 0}\nimg{height: 100%; width: auto}</style>"
              "<img id='a' src='/camera' onload='this.style.display=\"initial\"; var b = document.getElementById(\"b\"); b.style.display=\"none\"; b.src=\"camera?\"+Date.now(); '>"
              "<img id='b' style='display: none' src='/camera' onload='this.style.display=\"initial\"; var a = document.getElementById(\"a\"); a.style.display=\"none\"; a.src=\"camera?\"+Date.now(); '>");
            client.println();
            break;
          } 
          else 
          {
            currentLine = "";
          }
        } 
        else if (c != '\r') 
        {
          currentLine += c;
        }
        
        if(currentLine.endsWith("GET /camera"))
        {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:image/bmp");
            client.println();
            
            for(int i = 0; i < BMP::headerSize; i++)
               client.write(bmpHeader[i]);
            for(int i = 0; i < camera->xres * camera->yres * 2; i++)
               client.write(camera->frame[i]);
        }
      }
    }
    // close the connection:
    client.stop();
    //Serial.println("Client Disconnected.");
  }  
}

#endif

#ifdef WIFI_CONTROL_ON
#include <WiFi.h>

#define GROUND                        0x00
#define MINIMUM_TORQUE_CORRECTION     0x80

#define SPEED_PIN_HIGH                19
#define SPEED_PIN_LOW                 18


#define STEERING_PIN_HIGH             26 //17
#define STEERING_PIN_LOW              25 //16

#define ssid        "PiZeroCar"
#define password    "P1Password"


#define BUFSIZE 4

uint8_t buf[BUFSIZE]; //0-> 1-forward, 0 backward, 1-> speedVal, 3-> 0-left, 1-right, 4-> directionVal

WiFiServer server(80);

void setSpeed(char dir, char speed){
    #ifdef DEBUG_MODE
    Serial.print("Speed: ");
  #endif
  if (dir){
    ledcWrite(1,speed ? MINIMUM_TORQUE_CORRECTION + speed/2 : GROUND);
    ledcWrite(2,GROUND);
    #ifdef DEBUG_MODE
      Serial.println(int(speed));
    #endif
  } else {
     ledcWrite(2,speed? MINIMUM_TORQUE_CORRECTION + speed/2 : GROUND);
     ledcWrite(1,GROUND);
     #ifdef DEBUG_MODE
      Serial.println(-int(speed));
     #endif
  }
}

void setSteering(char dir, char steering){
  #ifdef DEBUG_MODE
    Serial.print("Steering: ");
  #endif
  if (dir){
    ledcWrite(3,steering/*?MINIMUM_TORQUE_CORRECTION + steering>>1:GROUND*/);
    ledcWrite(4,GROUND);
    #ifdef DEBUG_MODE
      Serial.println(int(steering));
    #endif
  } else {
     ledcWrite(4,steering/*?MINIMUM_TORQUE_CORRECTION + steering>>1:GROUND*/);
     ledcWrite(3,GROUND);
     #ifdef DEBUG_MODE
      Serial.println(-int(steering));
     #endif
  }
}

#endif







void setup() 
{
 #ifdef DEBUG_MODE
  Serial.begin(115200);

  Serial.print(millis());
  Serial.println("\t\t | BLECar started");
  #endif
   
  #ifdef BLUETOOTH_CONTROL_ON
  // Initialize DC Motor pins for PWM
  ledcAttachPin(SPEED_PIN_HIGH,1);
  ledcAttachPin(SPEED_PIN_LOW,2);
  ledcAttachPin(STEERING_PIN_HIGH,3);
  ledcAttachPin(STEERING_PIN_LOW,4);
  // Initialize channels 
  // channels 0-15, resolution 1-16 bits, freq limits depend on resolution
  // ledcSetup(uint8_t channel, uint32_t freq, uint8_t resolution_bits);
  ledcSetup(1, 18000, 8); // 18 kHz PWM, 8-bit resolution
  ledcSetup(2, 18000, 8);
  ledcSetup(3, 18000, 8); // 18 kHz PWM, 8-bit resolution
  ledcSetup(4, 18000, 8);


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
  #endif

  #ifdef WIFI_CAMERA_ON
  
  wifiMulti.addAP(ssid1, password1);
  Serial.println("Connecting Wifi...");
  if(wifiMulti.run() == WL_CONNECTED) {
      Serial.println("");
      Serial.println("WiFi connected");
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
  }
  
  camera = new OV7670(OV7670::Mode::QQVGA_RGB565, SIOD, SIOC, VSYNC, HREF, XCLK, PCLK, D0, D1, D2, D3, D4, D5, D6, D7);
  BMP::construct16BitHeader(bmpHeader, camera->xres, camera->yres);
  
  server.begin();
  
  #endif


  #ifdef WIFI_CONTROL_ON

  // Initialize DC Motor pins for PWM
  ledcAttachPin(SPEED_PIN_HIGH,1);
  ledcAttachPin(SPEED_PIN_LOW,2);
  ledcAttachPin(STEERING_PIN_HIGH,3);
  ledcAttachPin(STEERING_PIN_LOW,4);
  // Initialize channels 
  // channels 0-15, resolution 1-16 bits, freq limits depend on resolution
  // ledcSetup(uint8_t channel, uint32_t freq, uint8_t resolution_bits);
  ledcSetup(1, 18000, 8); // 18 kHz PWM, 8-bit resolution
  ledcSetup(2, 18000, 8);
  ledcSetup(3, 18000, 8); // 18 kHz PWM, 8-bit resolution
  ledcSetup(4, 18000, 8);
  
    Serial.println();
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(ssid);

    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    Serial.println("");
    Serial.println("WiFi connected.");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    
    server.begin();
  #endif
}

void loop()
{
  #ifdef WIFI_CAMERA_ON
  camera->oneFrame();
  serve();
  #endif

  #ifdef WIFI_CONTROL_ON
  WiFiClient client = server.available();   // listen for incoming clients

  if (client) {                             // if you get a client,
    Serial.print("New Client:");           // print a message out the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {   
        // if there's bytes to read from the client,
        client.read(buf,BUFSIZE);
        setSpeed(buf[0],buf[1]);
        setSteering(buf[2],buf[3]);
//        char speed = client.read();             // read a byte, then
//        char steering = client.read();             // read a byte, then
//        Serial.write(speed);                    // print it out the serial monitor
//        Serial.print(" ");
//        Serial.write(steering);
        client.write(buf,BUFSIZE);
        Serial.println();
    }
    }
    // close the connection:
    client.stop();
    Serial.println("Client Disconnected.");
  }
  #endif
  
  #ifdef BLUETOOTH_CONTROL_ON
  delay(2000);
  #endif
}
