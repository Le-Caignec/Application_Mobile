package com.mobile.sharedwallet.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.behavior.SwipeDismissBehavior
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.models.Depense
import com.mobile.sharedwallet.utils.Shared
import kotlin.math.abs


class DepenseAdapter(private var dataSet : ArrayList<Depense>, private val listView: ListView) : BaseAdapter() {

    override fun getCount(): Int = dataSet.size

    override fun getItem(position: Int): Depense = dataSet[position]

    override fun getItemId(p0: Int): Long = getItem(p0).title.hashCode().toLong()

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {
        val depense : Depense = getItem(position)

        val depenseView : View = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.depense_row, parent,false)

        // Fetch Tributaire photo
        depenseView.findViewById<ImageView>(R.id.depensePurchaserPhoto).setImageBitmap(
            try {
                Shared.pot.participants.first { it.uid == depense.whoPaid.uid }.photo
                    ?: parent.context.getDrawable(R.drawable.ic_baseline_account_circle_24)?.toBitmap()
            } catch (e: Exception) {
                parent.context.getDrawable(R.drawable.ic_baseline_account_circle_24)?.toBitmap()
            }
        )
        depenseView.findViewById<TextView>(R.id.depenseTitle).text = depense.title
        depenseView.findViewById<TextView>(R.id.depenseDescription).text = depense.depCategory
        depenseView.findViewById<TextView>(R.id.depenseMetaText).text = depense.amountPaid.toString()
            .plus(parent.context.getString(R.string.space))
            .plus(parent.context.getString(R.string.euro_symbol))

        return depenseView
    }

    fun add(`object`: Depense?) {
        dataSet.add(0, `object` ?: Depense())
        notifyDataSetChanged()
    }

    fun delete(dep : Depense) {
        dataSet.remove(dep)
        notifyDataSetChanged()
    }

    fun refresh(newList: ArrayList<Depense>) = run { dataSet = newList }

}