package com.mobile.sharedwallet.constants

object FirebaseConstants {

    val ONE_MEGABYTE : Long = 1024 * 1024
    val MAX_PROFILE_PHOTO_SIZE : Long = ONE_MEGABYTE

    object CollectionNames {
        val Pot : kotlin.String = "pot"

        val Users : kotlin.String = "users"

        val WaitingPot : kotlin.String = "waitingPot"
    }

    object StorageRef {
        val Pictures : kotlin.String = "pictures"
    }

    object String {
        val DisplayNameSeparator : kotlin.String = "|||||"
    }

}