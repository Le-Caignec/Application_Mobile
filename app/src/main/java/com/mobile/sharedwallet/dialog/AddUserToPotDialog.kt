package com.mobile.sharedwallet.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.models.*
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import com.mobile.sharedwallet.utils.Validate
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class AddUserToPotDialog(private val cagnotte : Cagnotte?, private var usersEmails : ArrayList<String> = ArrayList()) : DialogFragment() {

    private lateinit var store : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = FirebaseFirestore.getInstance()
    }

    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_user_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<MaterialButton>(R.id.addNewUserSubmit).setOnClickListener {
            if (validateEmail() && userExists()) addMember()
        }

        view.findViewById<MaterialButton>(R.id.addNewUserCancel).setOnClickListener {
            dismiss()
        }

        val emailsAdapter = ArrayAdapter(requireContext(), R.layout.autocomplete_row_item, R.id.autocompleteRow, usersEmails as List<*>)

        val newUserEmail = view.findViewById<AutoCompleteTextView>(R.id.newUserEmail)
        newUserEmail.threshold = 1
        newUserEmail.setAdapter(emailsAdapter)
    }

    private fun validateEmail() : Boolean {
        return view?.let {
            val email = it.findViewById<AutoCompleteTextView>(R.id.newUserEmail).text.toString()
            if (email == Shared.user?.email) {
                Toast.makeText(requireContext(), getString(R.string.message_warning_add_yourself), Toast.LENGTH_SHORT).show()
                return@let false
            } else if (!usersEmails.contains(email)) {
                Toast.makeText(requireContext(), getString(R.string.message_error_no_account), Toast.LENGTH_SHORT).show()
                return@let false
            }
            return@let Validate.checkEmailConditions(requireContext(), email)
        } ?: false
    }

    private fun userExists() : Boolean {
        return view?.let {
            val email = it.findViewById<AutoCompleteTextView>(R.id.newUserEmail).text.toString()
            return@let usersEmails.contains(email)
        } ?: false
    }

    private fun notAlreadyInPot(uid : String) : Boolean {
        return view?.let {
            if(Shared.pot.participants.map { it.uid }.contains(uid)) {
                return@let false
            }
            return@let true
        } ?: false
    }

    private fun addMember() {
        view?.let { view ->
            val email = view.findViewById<AutoCompleteTextView>(R.id.newUserEmail).text.toString()

            MainScope().launch {
                val participant : Participant? = fetchParticipantFromEmail(email)

                participant?.let { participant : Participant ->
                    if(notAlreadyInPot(participant.uid)) addParticipantToWaitingPot(participant)
                    else {
                        val dialog = MessageDialog(requireActivity())
                        dialog
                            .create(getString(R.string.message_warning_participant_already_pot))
                            .show()
                    }
                }
            }
        }
    }

    private suspend fun fetchParticipantFromEmail(email: String) : Participant? {
        return withContext(Dispatchers.Main) {
            try {
                val snapShot : QuerySnapshot = store
                    .collection(FirebaseConstants.CollectionNames.Users)
                    .whereEqualTo(User.Attributes.EMAIL.string, email)
                    .limit(1)
                    .get()
                    .await()

                if(!snapShot.isEmpty) {
                    return@withContext Utils.castUserToParticipant(snapShot.documents[0].toObject<User>() as User)
                }
                return@withContext null
            }
            catch (e : Exception) {
                return@withContext null
            }
        }
    }

    private fun addParticipantToWaitingPot(participant: Participant) {
        cagnotte?.let { cagnotte : Cagnotte ->
            store
                .collection(FirebaseConstants.CollectionNames.WaitingPot)
                .whereEqualTo(WaitingPot.Attributes.POT_REF.string, Utils.convertStringToRef(FirebaseConstants.CollectionNames.Pot, Shared.potRef))
                .limit(1)
                .get()
                .addOnSuccessListener {
                    try {
                        if((it.documents[0][WaitingPot.Attributes.WAITING_UID.string] as ArrayList<*>).contains(participant.uid)) {
                            Toast.makeText(requireContext(), participant.name.plus(getString(R.string.space)).plus(getString(R.string.message_warning_person_already_in_pot)).plus(getString(R.string.space)).plus(cagnotte.name), Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        it
                            .documents[0]
                            .reference
                            .update(
                                WaitingPot.Attributes.WAITING_UID.string,
                                FieldValue.arrayUnion(participant.uid)
                            )
                        Shared.pot.participants.add(participant)
                        Toast.makeText(requireContext(), getString(R.string.message_invitation_send).plus(getString(R.string.space)).plus(participant.name), Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.message_error_add_person), Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.message_error_add_person), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
        }
    }
}