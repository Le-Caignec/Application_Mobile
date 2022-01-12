package com.mobile.sharedwallet.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.fragment.PortalFragment
import com.mobile.sharedwallet.utils.Validate

class ResetPasswordDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reset_password_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<Button>(R.id.resetPasswordButton).setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        view?.let {
            val email = it.findViewById<EditText>(R.id.resetPasswordEmail).text.toString()
            if(!Validate.checkEmailConditions(requireContext(), email)) return@let

            FirebaseAuth
                .getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    val dialog = MessageDialog(requireActivity())
                        .navigateTo(PortalFragment(), false)
                        .create(getString(R.string.message_reset_password_email_sent))
                    dialog.show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}