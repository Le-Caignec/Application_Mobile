package com.mobile.sharedwallet.adapter

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.models.Tributaire
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils.Companion.castParticipantToTributaire

class DropListAdapter(private val participants : ArrayList<Participant>) : AdapterView.OnItemSelectedListener {

    fun generateSpinner (context : Context, view : View ){
        val spinner:Spinner= view.findViewById(R.id.spinnerPayeur)
        val spinnerArrayAdapter:ArrayAdapter<Participant> = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            participants
        )
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerArrayAdapter
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Shared.payeur = participants[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Shared.payeur = participants[0]
    }
}