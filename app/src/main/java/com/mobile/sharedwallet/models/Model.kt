package com.mobile.sharedwallet.models

interface Model {
    fun toFirebase() : HashMap<String, Any?>
}