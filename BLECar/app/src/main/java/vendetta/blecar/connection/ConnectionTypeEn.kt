package vendetta.blecar.connection

enum class ConnectionTypeEn(val value:String, val specific: String, val img:String) {
    WIFI_LOCAL("LAN Wi-Fi Connection", "Local IP Address:", "#000000"),
    WIFI_INET("Global Wi-Fi Connection", "Internet IP Address:", "#223344"),
    WIFI_AP("Direct Wi-Fi Access Point", "SSID:", "#ffffff"),
    MQTT("Message Queuing Telemetry Transport", "Shared Key:", "#ff00ff"),
    BLE("Bluetooth Low Energy", "Beacon Name:", "#00ffff")
}