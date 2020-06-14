package vendetta.picar.connection

data class ConnectionConfig(val name:String, val connType: ConnectionTypeEn, val connPlatform:ConnectionPlatformEn, val identifier: String, val addrValue:String, val secretValue:String){

    override fun toString(): String {
        return name
    }
}