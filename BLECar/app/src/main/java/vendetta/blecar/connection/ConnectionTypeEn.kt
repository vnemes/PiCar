package vendetta.blecar.connection

enum class ConnectionTypeEn(val value:String, val specific: String, val img:String) {
    WIFI_LOCAL("LAN Wifi Connection", "Local IP Address:", "#000000"),
    WIFI_INET("Global Wifi Connection", "Internet IP Address:", "#223344"),
    WIFI_AP("Direct WiFi Access Point", "SSID:", "#ffffff"),
    MQTT("Message Queuing Telemetry Transport", "Shared Key:", "#ff00ff"),
    BLE("Bluetooth Low Energy", "Beacon Name:", "#00ffff")
}