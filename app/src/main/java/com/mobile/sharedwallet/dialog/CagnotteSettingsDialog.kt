package com.mobile.sharedwallet.dialog

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.fragment.HomeFragment
import com.mobile.sharedwallet.models.Cagnotte
import com.mobile.sharedwallet.models.WaitingPot
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CagnotteSettingsDialog : DialogFragment() {

    private lateinit var store : FirebaseFirestore
    private lateinit var color : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        store = FirebaseFirestore.getInstance()
        color = Shared.pot.color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cagnotte_settings_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialButton>(R.id.submitSettings).setOnClickListener {
            updateCagnotte()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.cancelSettings).setOnClickListener { dismiss() }

        view.findViewById<TextView>(R.id.cagnotteSettingsCagnotteName).text = Shared.pot.name

        val colorPreview = view.findViewById<Chip>(R.id.cagnotteSettingsColorPreview)

        colorPreview.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(Shared.pot.color)))

        colorPreview.setOnClickListener {
            ColorPickerDialog(this).show(parentFragmentManager, "ColorPicker")
        }

        view.findViewById<EditText>(R.id.cagnotteSettingsDescription).text = SpannableStringBuilder(Shared.pot.description)

        view.findViewById<CardView>(R.id.cagnotteSettingsDelete).setOnClickListener {
            MessageDialog(requireActivity()) {
                try {
                    store
                        .collection(FirebaseConstants.CollectionNames.Pot)
                        .document(Shared.potRef)
                        .delete()
                        .await()

                    store
                        .collection(FirebaseConstants.CollectionNames.WaitingPot)
                        .whereEqualTo(WaitingPot.Attributes.POT_REF.string, Utils.convertStringToRef(FirebaseConstants.CollectionNames.Pot, Shared.potRef))
                        .get()
                        .await()
                        .forEach { doc ->
                            store
                                .collection(FirebaseConstants.CollectionNames.WaitingPot)
                                .document(doc.id)
                                .delete()
                        }


                    Shared.cagnottes.remove(Shared.potRef) as Any
                } catch (e : Exception) {}
            }
                .navigateTo(HomeFragment(), false)
                .create(getString(R.string.message_warning_delete))
                .setNeutralButton(getString(R.string.cancel), DialogInterface.OnClickListener { _, _ ->
                    dismiss()
                })
                .show()
        }
    }

    fun actualizeColor(color_ : String) {
        color = color_
        view?.findViewById<Chip>(R.id.cagnotteSettingsColorPreview)?.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(color_)))
    }


    private fun updateCagnotte() {
        Shared.overlay?.show()
        MainScope().launch {
            val uName : Boolean = updateName()
            val uColor : Boolean = updateColor()
            updateDescription()

            Shared.cagnotteFragment?.update(
                if(uColor) Shared.pot.color else null,
                if(uName) Shared.pot.name else null
            )
            Shared.overlay?.hide()
        }
    }

    private suspend fun updateName() : Boolean {
        return view?.let { v : View ->
            val newName = v.findViewById<TextView>(R.id.cagnotteSettingsCagnotteName).text.toString()
            return@let if (Shared.pot.name != newName && newName.isNotEmpty()) {
                try {
                    store
                        .collection(FirebaseConstants.CollectionNames.Pot)
                        .document(Shared.potRef)
                        .update(Cagnotte.Attributes.NAME.string, newName)
                        .await()

                    Shared.pot.name = newName
                    true
                } catch (e : Exception) { false }
            } else false
        } ?: false
    }

    private suspend fun updateColor() : Boolean {
        return if (Shared.pot.color != color && color.isNotEmpty()) {
            try {
                store
                    .collection(FirebaseConstants.CollectionNames.Pot)
                    .document(Shared.potRef)
                    .update(Cagnotte.Attributes.COLOR.string, color)
                    .await()

                Shared.pot.color = color
                true
            } catch (e : Exception) { false }
        } else false
    }


    private suspend fun updateDescription() {
        view?.let { view ->
            val desc = view.findViewById<EditText>(R.id.cagnotteSettingsDescription).text.toString()

            if (Shared.pot.description != desc) {
                try {
                    store
                        .collection(FirebaseConstants.CollectionNames.Pot)
                        .document(Shared.potRef)
                        .update(Cagnotte.Attributes.DESCRIPTION.string, desc)
                        .await()

                    Shared.pot.description = desc
                } catch (e : Exception) { }
            }
        }
    }
}