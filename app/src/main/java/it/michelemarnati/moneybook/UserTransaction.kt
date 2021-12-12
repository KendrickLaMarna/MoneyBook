package it.michelemarnati.moneybook

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class UserTransaction {
    lateinit var date: String
    lateinit var description:String
    var import:Float
    lateinit var type:String


    constructor(){
        date = SimpleDateFormat("dd/MM/yyyy").format(Date())
        description = ""
        import = 0.0f
        type = ""

    }

    constructor(date: String, description: String, import: Float, type: String) {
        this.date = date
        this.description = description
        this.import = import
        this.type = type
    }


}