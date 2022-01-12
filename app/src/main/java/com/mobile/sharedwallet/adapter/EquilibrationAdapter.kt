package com.mobile.sharedwallet.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.utils.Shared

class EquilibrationAdapter(private val dataSet : ArrayList<ArrayList<String>> = ArrayList()) : BaseAdapter() {

    override fun getCount(): Int = dataSet.size

    override fun getItem(p0: Int): ArrayList<String> = dataSet[p0]

    override fun getItemId(p0: Int): Long {
        val item = getItem(p0)
        return item[0].plus(item[1]).plus(item[2]).hashCode().toLong()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {
        val view = p1 ?: LayoutInflater.from(parent?.context).inflate(R.layout.equilibration_row, parent, false)

        val item = getItem(p0)

        val participant1 : Participant = Shared.pot.participants.first { it.uid == item[0] }
        view.findViewById<ImageView>(R.id.equilibrationRowDebterPhoto).setImageBitmap(
            participant1.photo
                ?: parent?.context?.getDrawable(R.drawable.ic_baseline_account_circle_24)?.toBitmap()
        )

        val participant2 : Participant = Shared.pot.participants.first { it.uid == item[1] }
        view.findViewById<ImageView>(R.id.equilibrationRowReceiverPhoto).setImageBitmap(
            participant2.photo
                ?: parent?.context?.getDrawable(R.drawable.ic_baseline_account_circle_24)?.toBitmap()
        )

        view.findViewById<TextView>(R.id.equilibrationRowTextDebter).text = participant1.name
        view.findViewById<TextView>(R.id.equilibrationRowTextReceiver).text =  participant2.name
        view.findViewById<TextView>(R.id.equilibrationRowDebt).text = item[2]
            .plus(parent?.context?.getString(R.string.space))
            .plus(parent?.context?.getString(R.string.euro_symbol))

        return view
    }

    fun add(newData : ArrayList<String>) {
        dataSet.add(newData)
        notifyDataSetChanged()
    }
}