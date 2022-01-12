package com.mobile.sharedwallet.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class GoogleSignInContract : ActivityResultContract<Any, Unit>() {

    override fun createIntent(
        context: Context,
        input: Any?
    ): Intent = Intent(context, GoogleSignInActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?) : Unit {
        return
    }
}