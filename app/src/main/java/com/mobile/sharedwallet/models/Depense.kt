package com.mobile.sharedwallet.models

import com.google.firebase.Timestamp

// Users are stored with their uID, that is why there are strings
data class Depense (
    val title : String,
    val whoPaid : Participant,
    val amountPaid : Float,
    val forWho : List<Tributaire>,
    val creationDate : Timestamp,
    val depCategory: String
    ) : Model {

    enum class Attributes(val string: String) {
        TITLE("title"),
        WHO_PAID("whoPaid"),
        AMOUNT_PAID("amountPaid"),
        FOR_WHO("forWho"),
        CREATION_DATE("creationDate"),
        DEP_CATEGORY("depCategory")
    }

    constructor() : this("", Participant(), 0.0f, ArrayList<Tributaire>(), Timestamp(0, 0), "Others")

    override fun toFirebase() : HashMap<String, Any?> {
        return hashMapOf(
            Attributes.TITLE.string to title,
            Attributes.WHO_PAID.string to whoPaid.toFirebase(),
            Attributes.AMOUNT_PAID.string to amountPaid,
            Attributes.FOR_WHO.string to forWho.map { it.toFirebase() },
            Attributes.CREATION_DATE.string to creationDate,
            Attributes.DEP_CATEGORY.string to depCategory
        )
    }
}