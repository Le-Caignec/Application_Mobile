package com.mobile.sharedwallet.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.models.Cagnotte

class CagnotteAdapter(private var dataSet : HashMap<String, Cagnotte>, private val callback : (String) -> Unit) : BaseAdapter() {


    private var mKeys : ArrayList<String> = ArrayList()

    init {
        mKeys.addAll(dataSet.keys)
    }


    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(p0: Int): Cagnotte {
        val key : String = mKeys[p0]
        return dataSet[key] ?: Cagnotte()
    }

    override fun getItemId(p0: Int): Long {
        return mKeys[p0].hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {

        val cagnotteView : View = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.cagnotte_row, parent,false)

        val cagnotte = getItem(position)


        cagnotteView.findViewById<MaterialCardView>(R.id.cagnottePreview).setCardBackgroundColor(Color.parseColor(cagnotte.color))
        cagnotteView.findViewById<TextView>(R.id.cardTextPreview).text = cagnotte.name

        cagnotteView.findViewById<TextView>(R.id.cardTextDescription).text = cagnotte.description

        val totalSpentAmountPreview = cagnotteView.findViewById<TextView>(R.id.totalSpentAmountPreview)

        var sum = 0f
        cagnotte.totalSpent.forEach { sum += it.amountPaid }

        totalSpentAmountPreview?.text = sum
            .toString()
            .plus(parent.context.getString(R.string.space))
            .plus(parent.context.getString(R.string.euro_symbol))

        cagnotteView.setOnClickListener{
            callback.invoke(mKeys[position])
        }

        return cagnotteView
    }

    fun add(data : Pair<String, Cagnotte>) {
        dataSet[data.first] = data.second
        if(dataSet.size > 1) {
            dataSet = dataSet.toList().sortedByDescending { (_, v) -> v.creationDate }.toMap() as HashMap<String, Cagnotte>
        }
        mKeys.clear()
        mKeys.addAll(dataSet.keys)
        notifyDataSetChanged()
    }
}