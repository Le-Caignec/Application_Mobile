package com.mobile.sharedwallet.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mobile.sharedwallet.R


class CategoryAdapter(private var dataSet : HashMap<String, Float>) : BaseAdapter() {

    private var mKeys : ArrayList<String> = ArrayList()
    private var sumTotal = 0f

    init {

        if(dataSet.size > 1) {
            dataSet = dataSet.toList().sortedByDescending { (_, v) -> v}.toMap() as HashMap<String, Float>
        }

        mKeys.addAll(dataSet.keys)
        dataSet.forEach{
            sumTotal += it.value
        }
        if (sumTotal == 0f){
            sumTotal = 1.0f
        }
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(p0: Int): Float {
        val key : String = mKeys[p0]
        return dataSet[key] ?: 0f
    }

    override fun getItemId(p0: Int): Long {
        return mKeys[p0].hashCode().toLong()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun getView(p0: Int, convertView: View?, parent: ViewGroup?): View? {

        val categoryView : View = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.category_row, parent,false)
        val category = mKeys[p0]
        val valeur = getItem(p0)

        when(category){
            "Others" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.others)
            "Groceries" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.groceries)
            "Entertainment" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.entertainment)
            "Shopping" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.shopping)
            "Transportation" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.transportation)
            "Rent" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.rent)
            "Insurances" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.insurances)
            "Restaurant" -> categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.restaurant)
            else ->{
                categoryView.findViewById<ImageView>(R.id.categoryPhoto).setImageResource(R.drawable.others)
            }
        }

        categoryView.findViewById<TextView>(R.id.categoryTitle).text = category
        categoryView.findViewById<ProgressBar>(R.id.categoryProgress).progress = (100*valeur/sumTotal).toInt()
        categoryView.findViewById<TextView>(R.id.categoryInfo).text = "%.1f".format((100*valeur/sumTotal))+" % of total expenses"
        if (parent != null) {
            categoryView.findViewById<TextView>(R.id.categoryMetaText).text = valeur.toString()
                .plus(parent.context.getString(R.string.space))
                .plus(parent.context.getString(R.string.euro_symbol))
        }

        return categoryView
    }

}