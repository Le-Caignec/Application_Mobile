package com.mobile.sharedwallet.dialog

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobile.sharedwallet.MainActivity
import com.mobile.sharedwallet.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MessageDialog (
    private val activity: Activity,
    private val callback: (suspend () -> Any)? = null) {

    private var navigateToFragment : Fragment? = null
    private var addToStackBack : Boolean? = null

    fun navigateTo(fragment: Fragment, possibleReturn : Boolean? = null) : MessageDialog {
        navigateToFragment = fragment
        possibleReturn?.let { addToStackBack = it }
        return this
    }

    fun create(message : String) : AlertDialog.Builder {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
            .setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                MainScope().launch {
                    callback?.invoke()
                    navigate()
                }
            }
        // Create the AlertDialog object and return it
        builder.create()
        return builder
    }

    fun verifyAccountDialog(fragment : Fragment? = null, possibleReturn : Boolean? = null): AlertDialog.Builder {
        fragment?.let { navigateTo(fragment, possibleReturn ?: true) }
        val builder : AlertDialog.Builder = create(
            activity.getString(R.string.message_please_verify_account_before)
        )

        builder.setNeutralButton(activity.getString(R.string.re_send_email)) { _, _ ->
            Firebase
                .auth
                .currentUser?.let {
                    it.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(activity, activity.getString(R.string.email_sent), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, activity.getString(R.string.message_email_not_send), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        builder.setOnDismissListener { _ ->
            navigateToFragment?.let { (activity as MainActivity).replaceFragment(it, addToStackBack ?: true) }
        }

        return builder
    }

    private fun navigate() {
        navigateToFragment?.let {
            (activity as MainActivity).replaceFragment(
                it,
                addToStackBack ?: true
            )
        }

    }
}
