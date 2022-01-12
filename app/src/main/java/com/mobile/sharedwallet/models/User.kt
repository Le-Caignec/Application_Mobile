package com.mobile.sharedwallet.models

import android.graphics.Bitmap

data class User(
    val uid : String?,
    var firstName : String?,
    var lastName : String?,
    val email : String?,
    var photo : Bitmap?,
) : Model {
    constructor() : this(null, null, null, null, null)

    enum class Attributes(val string: String) {
        UID("uid"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName"),
        EMAIL("email"),
    }

    fun isNullOrEmpty() : Boolean {
        return uid.isNullOrEmpty()
                && firstName.isNullOrEmpty()
                && lastName.isNullOrEmpty()
                && email.isNullOrEmpty()
                && photo == null
    }

    override fun toFirebase(): HashMap<String, Any?> {
        return hashMapOf(
            Attributes.UID.string to uid,
            Attributes.FIRST_NAME.string to firstName,
            Attributes.LAST_NAME.string to lastName,
            Attributes.EMAIL.string to email
        )
    }
}
