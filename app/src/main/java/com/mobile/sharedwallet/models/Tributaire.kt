package com.mobile.sharedwallet.models

class Tributaire (
    val name : String,
    val uid : String,
    var cout : Float,
    var percentageCout : Float,
    var selected : Boolean
) : Model {

    enum class Attributes(val string: String) {
        NAME("name"),
        UID("uid"),
        COUT("cout"),
        PERCENTAGE_COUT("percentageCout")
    }

    constructor() : this("","",0f,0f,false)

    override fun toFirebase(): HashMap<String, Any?> {
        return hashMapOf(
            Attributes.NAME.string to name,
            Attributes.UID.string to uid,
            Attributes.COUT.string to cout,
            Attributes.PERCENTAGE_COUT.string to percentageCout
        )
    }

    override fun toString(): String {
        return name
    }
}