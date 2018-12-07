package vendetta.blecar.connection

data class ConnectionConfig(val name:String, val connType: ConnectionTypeEn, val identifier: String){

    override fun toString(): String {
        return name
    }
}