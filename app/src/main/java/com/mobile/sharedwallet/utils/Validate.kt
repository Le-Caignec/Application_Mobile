package com.mobile.sharedwallet.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.mobile.sharedwallet.R
import java.util.regex.Pattern

class Validate {

    companion object {
        fun checkPasswordConditions(context : Context, password1 : String?, password2 : String?) : Boolean? {
            val passwordPattern = "^(?=.*\\d)(?=.*[A-Z])[0-9a-zA-Z]{4,}$"

            return if (password1.isNullOrEmpty() || password2.isNullOrEmpty()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.message_password_not_empty),
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else if (password1 != password2) {
                Toast.makeText(
                    context,
                    context.getString(R.string.message_passwords_must_match),
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else if (!Pattern.compile(passwordPattern).matcher(password1).matches()
                || !Pattern.compile(passwordPattern).matcher(password2).matches()
            ) {
                Toast.makeText(
                    context,
                    context.getString(R.string.message_password_conditions),
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else null
        }

        fun checkEmailConditions(context : Context, email : String?) : Boolean {
            return email?.let {
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, context.getString(R.string.message_email_not_good_format), Toast.LENGTH_SHORT).show()
                    return@let false
                }
                return@let true
            } ?: false
        }
    }
}