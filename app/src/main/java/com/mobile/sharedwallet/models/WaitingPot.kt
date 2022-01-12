package com.mobile.sharedwallet.models

import com.google.firebase.firestore.DocumentReference

class WaitingPot(
    private val potRef : DocumentReference?,
    private val waitingUID : ArrayList<String>
) : Model {

    constructor() : this(null, ArrayList())

    enum class Attributes(val string : String) {
        POT_REF("potRef"),
        WAITING_UID("waitingUID")
    }

    override fun toFirebase(): HashMap<String, Any?> {
        return hashMapOf(
            Attributes.POT_REF.string to potRef,
            Attributes.WAITING_UID.string to waitingUID
        )
    }
}