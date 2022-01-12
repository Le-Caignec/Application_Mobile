package com.mobile.sharedwallet.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.SlideDistanceProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.fragment.HomeFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.Depense
import com.mobile.sharedwallet.models.WaitingPot
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.tasks.await

class DepenseDialog(private val depense : Depense) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.depense_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<TextView>(R.id.depenseDialogTitle).text = depense.title
        view.findViewById<TextView>(R.id.depenseDialogCreationDate).text = Utils.dateFormatter(depense.creationDate)
        view.findViewById<TextView>(R.id.depenseDialogCategory).text = depense.depCategory
        view.findViewById<TextView>(R.id.depenseDialogPayedBy).text = depense.whoPaid.name
        view.findViewById<TextView>(R.id.depenseDialogAmountPaid).text = depense.amountPaid.toString().plus(getString(R.string.euro_symbol))
        view.findViewById<ListView>(R.id.depenseDialogForWho).adapter = ArrayAdapter(requireContext(), R.layout.for_who_textview, depense.forWho)

        val delete = view.findViewById<CardView>(R.id.depenseDialogDelete)

        delete.visibility = if(depense.whoPaid.uid == Shared.user?.uid) View.VISIBLE else View.INVISIBLE

        delete.setOnClickListener {
            MessageDialog(requireActivity()) {
                try {

                    val totalSpent = ArrayList<Depense>(Shared.pot.totalSpent)

                    FirebaseFirestore
                        .getInstance()
                        .collection(FirebaseConstants.CollectionNames.Pot)
                        .document(Shared.potRef)
                        .update(Cagnotte.Attributes.TOTAL_SPENT.string, totalSpent.remove(depense))


                    Shared.pot.totalSpent.remove(depense)
                } catch (e : Exception) {}
            }
                .create(getString(R.string.message_warning_delete))
                .setNeutralButton(getString(R.string.cancel), DialogInterface.OnClickListener { _, _ ->
                    dismiss()
                })
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }
}