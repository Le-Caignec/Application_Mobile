package com.mobile.sharedwallet.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.dialog.MessageDialog
import com.mobile.sharedwallet.models.User
import com.mobile.sharedwallet.utils.Overlay
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import com.mobile.sharedwallet.utils.Validate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RegisterFragment: Fragment() {

    private lateinit var auth : FirebaseAuth
    private var user : FirebaseUser? = null

    private var firstName : String? = null
    private var lastName : String? = null
    private var email : String? = null
    private var password1 : String? = null
    private var password2 : String? = null

    private var overlay : Overlay? = Shared.overlay

    /**
     * Affect firebase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    /**
     * Bind buttons
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.register_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.createAccount).setOnClickListener {
            if (validate()) {
                createAccount()
            }
        }
    }

    /**
     * Validate form
     *
     * @return true if everything is OK, false else
     */
    private fun validate() : Boolean {

        view?.let { view : View ->
            firstName = view.findViewById<EditText>(R.id.firstNameEditText).text.toString()
            lastName = view.findViewById<EditText>(R.id.lastNameEditText).text.toString()
            email = view.findViewById<EditText>(R.id.emailEditText).text.toString()
            password1 = view.findViewById<EditText>(R.id.passwordEditText).text.toString()
            password2 = view.findViewById<EditText>(R.id.password2EditText).text.toString()
        }

        // Check form conditions before to create an account
        if (firstName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.message_first_name_must_not_empty), Toast.LENGTH_SHORT).show()
            return false
        }
        else if (lastName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.message_last_name_must_not_empty), Toast.LENGTH_SHORT).show()
            return false
        }
        else if (email.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email as CharSequence).matches()) {
            Toast.makeText(requireContext(), getString(R.string.message_enter_valid_email), Toast.LENGTH_SHORT).show()
            return false
        }
        else {
            val condition : Boolean? = Validate.checkPasswordConditions(requireContext(), password1, password2)
            if (condition != null) return condition
        }

        return true
    }

    /**
     * Create authentication account
     */
    private fun createAccount() {
        overlay?.show()
        auth
            .createUserWithEmailAndPassword(email!!, password1!!)
            .addOnSuccessListener {
                user = it.user
                sendVerificationEmail()
            }
            .addOnFailureListener {
                overlay?.hide()
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Send verification e-mail
     */
    private fun sendVerificationEmail() {
        user?.let { user : FirebaseUser ->
            user
                .sendEmailVerification()
                .addOnSuccessListener {
                    submitInfos()
                }
                .addOnFailureListener {
                    overlay?.hide()
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Update user data in Firebase
     */
    private fun submitInfos() {
        user?.let { user : FirebaseUser ->
            user
                .updateProfile(userProfileChangeRequest {
                    displayName = Utils.getDisplayNameFromFirstnameAndLastName(firstName, lastName)
                })
                .addOnSuccessListener {
                    createUser()
                }
                .addOnFailureListener {
                    overlay?.hide()
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createUser() {
        user?.let {
            MainScope().launch {
                val myUser : User = Utils.createUserFromFirebaseUser(user, false)

                Shared.user = myUser

                FirebaseFirestore
                    .getInstance()
                    .collection(FirebaseConstants.CollectionNames.Users)
                    .add(myUser.toFirebase())
                    .addOnSuccessListener {
                        overlay?.hide()
                        val dialog = MessageDialog(requireActivity())
                        dialog
                            .navigateTo(HomeFragment(), false)
                            .create(getString(R.string.message_confirmation_email_sent))
                            .show()
                    }
                    .addOnFailureListener {
                        overlay?.hide()
                        Toast.makeText(requireContext(), getString(R.string.message_error_create_profile), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
