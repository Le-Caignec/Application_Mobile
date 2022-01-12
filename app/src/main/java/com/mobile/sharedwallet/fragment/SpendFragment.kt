package com.mobile.sharedwallet.fragment

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialFade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.adapter.DepenseAdapter
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.dialog.*
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.Depense
import com.mobile.sharedwallet.models.User
import com.mobile.sharedwallet.utils.Overlay
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList


class SpendFragment : Fragment() {

    private lateinit var store : FirebaseFirestore
    private var cagnotte : Cagnotte? = null
    private lateinit var adapter : DepenseAdapter
    private val overlay : Overlay? = Shared.overlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        store = FirebaseFirestore.getInstance()
        cagnotte = Shared.pot
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.spend_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FloatingActionButton>(R.id.newSpendButton).setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                NewSpendDialog(this).show(parentFragmentManager, "NewSpendFragment")
            }
            else {
                val dialog = MessageDialog(requireActivity())
                    .verifyAccountDialog()
                dialog.show()
            }
        }

        view.findViewById<FloatingActionButton>(R.id.addPerson).setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                CoroutineScope(Dispatchers.Main).launch {
                    val usersEmails = fetchUsersEmails()
                    AddUserToPotDialog(cagnotte, usersEmails).show(parentFragmentManager, "AddUserToPotDialog")
                }
            }
            else {
                val dialog = MessageDialog(requireActivity())
                    .verifyAccountDialog()
                dialog.show()
            }
        }

        view.findViewById<FloatingActionButton>(R.id.cagnotteSettings).visibility = if(cagnotte?.createdBy == Shared.user?.uid) View.VISIBLE else View.INVISIBLE

        view.findViewById<FloatingActionButton>(R.id.cagnotteSettings).setOnClickListener {
            CagnotteSettingsDialog().show(parentFragmentManager, "CagnotteSettingsDialog")
        }

        val listViewDepense =  view.findViewById<ListView>(R.id.spends)
        adapter = DepenseAdapter(cagnotte!!.totalSpent, listViewDepense)
        listViewDepense.adapter = adapter

        listViewDepense.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position).let {
                DepenseDialog(it).show(parentFragmentManager, "DialogFragment".plus(position))
            }
        }


        // Getting SwipeContainerLayout
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.spendFragmentSwipeContainer)

        // Adding Listener
        swipe.setOnRefreshListener {
            overlay?.show()
            MainScope().launch {
                reloadDepense()
                adapter.notifyDataSetChanged()
                swipe.isRefreshing = false
                overlay?.hide()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }


    private suspend fun fetchUsersEmails() : ArrayList<String> {
        return withContext(Dispatchers.Main) {
            try {
                return@withContext store
                    .collection(FirebaseConstants.CollectionNames.Users)
                    .get()
                    .await()
                    .map { d -> d[User.Attributes.EMAIL.string].toString() }
                        as ArrayList<String>
            }
            catch (e : Exception) {
                return@withContext ArrayList<String>()
            }
        }
    }


    fun actualizeListDepenses(dep : Depense, add : Boolean) {
        if (add) adapter.add(dep)
        else adapter.delete(dep)
    }


    private suspend fun reloadDepense() {
        val cagnotte : Cagnotte = try {
            val data = store
                .collection(FirebaseConstants.CollectionNames.Pot)
                .document(Shared.potRef)
                .get()
                .await()
                .toObject<Cagnotte>() ?: Cagnotte()
            Shared.pot = data
            data
        } catch (e : Exception) { Cagnotte() }

        cagnotte.totalSpent.sortByDescending { it.creationDate }

        Shared.pot.participants.forEach {
            it.photo = Utils.fetchPhoto(it.uid)
        }
        adapter.refresh(cagnotte.totalSpent)
    }
}