package com.mobile.sharedwallet.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.mobile.sharedwallet.MainActivity
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.fragment.HomeFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.User
import com.mobile.sharedwallet.models.WaitingPot
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InvitationDialog(
    private val activity : MainActivity,
    private val user : User,
    private val cagnotteRef : String,
    private val waitingPotRef : String,
    private val isLast : Boolean
) : DialogFragment() {

    private lateinit var store : FirebaseFirestore
    private var cagnotte : Cagnotte? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = FirebaseFirestore.getInstance()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.invitation_dialog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        MainScope().launch {
            cagnotte = fetchCagnotte()
            cagnotte?.let { view.findViewById<TextView>(R.id.invitationCardText).text = getString(R.string.have_been_invited).plus(getString(R.string.space)).plus(it.name) }
        }

        view.findViewById<MaterialButton>(R.id.refuseInvitation).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.acceptInvitation).setOnClickListener {
            MainScope().launch {
                accept()
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return super.onCreateDialog(savedInstanceState)
    }


    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }


    override fun onDismiss(dialog: DialogInterface) {
        MainScope().launch {
            withContext(Dispatchers.Main) {
                try {
                    store
                        .document(Utils.convertStringToRef(FirebaseConstants.CollectionNames.WaitingPot, waitingPotRef).path)
                        .update(WaitingPot.Attributes.WAITING_UID.string, FieldValue.arrayRemove(user.uid))
                }
                catch(e : Exception) {}
                quit()
            }

        }
    }


    private suspend fun fetchCagnotte() : Cagnotte? {
        return withContext(Dispatchers.Main) {
            try {
                return@withContext store
                    .document(cagnotteRef)
                    .get()
                    .await()
                    .toObject<Cagnotte>()

            } catch (e : Exception) {
                return@withContext null
            }
        }

    }


    private suspend fun accept() {
        cagnotte?.let { cagnotte: Cagnotte ->
            user.uid?.let { uid : String ->
                withContext(Dispatchers.Main) {
                    try {
                        cagnotte.uids.add(uid)
                        cagnotte.participants.add(Utils.castUserToParticipant(user))

                        store
                            .document(cagnotteRef)
                            .update(cagnotte.toFirebase())

                    } finally {
                        // Delete Waiting ID in waitingPot anyway
                        dismiss()
                    }
                }
            }
        }
    }

    private fun quit() {
        if (isLast) {
            MainScope().launch {
                Utils.loadCagnotteList()
                activity.replaceFragment(HomeFragment(), false)
            }
        }
    }
}