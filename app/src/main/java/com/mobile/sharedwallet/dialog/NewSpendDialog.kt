package com.mobile.sharedwallet.dialog

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.adapter.DropListAdapter
import com.mobile.sharedwallet.adapter.TributairesAdapter
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.fragment.SpendFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.Depense
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.models.Tributaire
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import com.mobile.sharedwallet.utils.Utils.Companion.castParticipantListToTributaireList
import java.math.BigDecimal
import java.math.RoundingMode

class NewSpendDialog(private val parentFrag : SpendFragment) : DialogFragment() {

    private lateinit var store : FirebaseFirestore;
    private var participants : ArrayList<Participant>? = null
    private var price = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = FirebaseFirestore.getInstance()
        participants = Shared.pot.participants
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.newspend_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerlistview = view.findViewById<RecyclerView>(R.id.recyclerListView)
        recyclerlistview.layoutManager = LinearLayoutManager(activity)

        val particiantAdapter = TributairesAdapter(castParticipantListToTributaireList(participants!!),view)
        recyclerlistview.adapter = particiantAdapter

        view.findViewById<MaterialButton>(R.id.saveButton).setOnClickListener {
            if ((price != 0f) && (particiantAdapter.peopleSelected().size != 0)){
                saveNewSpend(particiantAdapter)
            }else if((price == 0f) && (particiantAdapter.peopleSelected().size != 0)) {
                val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                builder1.setMessage("You have not entered any price")
                builder1.setCancelable(true)
                builder1.create().show()
            }else if((particiantAdapter.peopleSelected().size == 0) && (price != 0f)) {
                val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                builder1.setMessage("You have not entered any person")
                builder1.setCancelable(true)
                builder1.create().show()
            }else if ((price == 0f) && (particiantAdapter.peopleSelected().size == 0)) {
                val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                builder1.setMessage("You have not entered any person and any people")
                builder1.setCancelable(true)
                builder1.create().show()
            }
        }

        //Spinner Payeur
        var spinnerAdapter = DropListAdapter(participants!!)
        spinnerAdapter.generateSpinner(requireContext(), view)

        //Price real time
        view.findViewById<EditText>(R.id.montant).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.findViewById<EditText>(R.id.montant).text.toString() != "") {
                    price = view.findViewById<EditText>(R.id.montant).text.toString().toFloat()
                    particiantAdapter.updatePrice(price)
                }else{
                    price = 0f
                    particiantAdapter.updatePrice(price)
                }
            }
        })


        view.findViewById<MaterialButton>(R.id.newSpendDialogCancel).setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())

    }

    private fun updateAllSoldes(tributaireSelected: ArrayList<Tributaire>) {
        val payeur : Participant = Shared.payeur ?: Participant()
        val updatedSolde = participants ?: ArrayList()
        var sum = 0.0
        for (soldes in updatedSolde) {
            for (tributaire in tributaireSelected) {
                if (soldes.uid == tributaire.uid && tributaire.uid!=payeur.uid) {
                    sum += tributaire.cout.toDouble()
                    soldes.solde -= BigDecimal(tributaire.cout.toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                }
            }
        }
        updatedSolde.forEach { soldes ->
            if (soldes.uid == payeur.uid) {
                soldes.solde += BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).toFloat()
            }
        }
        store
            .collection(FirebaseConstants.CollectionNames.Pot)
            .document(Shared.potRef)
            .update(Cagnotte.Attributes.PARTICIPANTS.string, updatedSolde.map { it.toFirebase() })
            .addOnFailureListener {
                Toast.makeText(requireActivity(), getString(R.string.message_error_update_balances), Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveNewSpend(adapter: TributairesAdapter) {
        view?.let {
            val title : String = it.findViewById<EditText>(R.id.title).text.toString()
            val categ : String = it.findViewById<Spinner>(R.id.spinnerCategory).selectedItem.toString()
            val selectedTributaire = adapter.peopleSelected()
            val depense = Depense(title, Shared.payeur ?: Participant(), price, selectedTributaire, Timestamp.now(),categ)

            store
                .collection(FirebaseConstants.CollectionNames.Pot)
                .document(Shared.potRef)
                .update(Cagnotte.Attributes.TOTAL_SPENT.string, FieldValue.arrayUnion(depense.toFirebase()))
                .addOnSuccessListener {
                    updateAllSoldes(selectedTributaire)
                    parentFrag.actualizeListDepenses(depense, true)
                    dismiss()
                }
                .addOnFailureListener {
                    dismiss()
                    Toast.makeText(requireActivity(), getString(R.string.message_error_add_new_spend), Toast.LENGTH_SHORT).show()
                }
        }
    }

}