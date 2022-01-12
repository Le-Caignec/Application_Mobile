package com.mobile.sharedwallet.models

import com.google.firebase.Timestamp

data class Cagnotte(
    val uids : ArrayList<String>,
    var name: String,
    var color : String,
    var description : String,
    val creationDate: Timestamp,
    val createdBy : String,
    var totalSpent: ArrayList<Depense>,
    val participants: ArrayList<Participant>,
    ) : Model {

    enum class Attributes(val string: String) {
        UIDS("uids"),
        NAME("name"),
        COLOR("color"),
        DESCRIPTION("description"),
        CREATION_DATE("creationDate"),
        CREATED_BY("createdBy"),
        TOTAL_SPENT("totalSpent"),
        PARTICIPANTS("participants")
    }

    constructor() : this(ArrayList(), "", "", "", Timestamp(0, 0), "", ArrayList<Depense>(), ArrayList<Participant>())

    fun isEmpty() : Boolean {
        return uids.isEmpty()
                && name == ""
                && color == ""
                && description == ""
                && creationDate == Timestamp(0, 0)
                && createdBy == ""
                && totalSpent.isEmpty()
                && participants.isEmpty()
    }

    override fun toFirebase() : HashMap<String, Any?> {
        return hashMapOf(
            Attributes.UIDS.string to uids,
            Attributes.NAME.string to name,
            Attributes.COLOR.string to color,
            Attributes.DESCRIPTION.string to description,
            Attributes.CREATION_DATE.string to creationDate,
            Attributes.CREATED_BY.string to createdBy,
            Attributes.TOTAL_SPENT.string to totalSpent.map { it.toFirebase() },
            Attributes.PARTICIPANTS.string to participants.map { it.toFirebase()}
        )
    }

}