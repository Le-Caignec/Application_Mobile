package com.mobile.sharedwallet.utils

import android.graphics.Bitmap
import java.util.HashMap

class CachePhoto {

    private var cache : HashMap<String, Bitmap> = HashMap()

    fun get(key: String) : Bitmap? {
        return if (cache.containsKey(key)) cache[key] else null
    }

    fun put(key : String, value : Bitmap) {
        cache[key] = value
    }

}