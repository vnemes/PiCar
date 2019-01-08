package vendetta.picar.connection

import vendetta.picar.R

enum class ConnectionTypeEn(val value:String, val specific: String, val img: Int) {
    WIFI_LOCAL("LAN Wi-Fi Connection", "Local IP Address:", R.drawable.wifi_local_logo),
    WIFI_INET("Global Wi-Fi Connection", "Internet IP Address:", R.drawable.wifi_global_logo),
    WIFI_AP("Direct Wi-Fi Access Point", "SSID:", R.drawable.wifi_ap_logo),
    MQTT("Message Queuing Telemetry Transport", "Shared Key:", R.drawable.wifi_local_mqtt),
    BLE("Bluetooth Low Energy", "Beacon Name:", R.drawable.wifi_local_ble)
}