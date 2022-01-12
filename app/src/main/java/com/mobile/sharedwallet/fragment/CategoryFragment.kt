package com.mobile.sharedwallet.fragment

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.adapter.CategoryAdapter
import com.mobile.sharedwallet.adapter.DepenseAdapter
import com.mobile.sharedwallet.models.Depense
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import java.text.DecimalFormat

class CategoryFragment : Fragment() {
    private var depenseListCopy : ArrayList<Depense> = ArrayList()
    private var categoryTotal : HashMap<String, Float> = hashMapOf<String, Float>()
    private var sumTotal : Int = 0
    private lateinit var adapter : CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.category_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        depenseListCopy = toParticipantList(participantListToString(Shared.pot.totalSpent))
        fillCategoryMap()

        val listViewCat =  view.findViewById<ListView>(R.id.listCategories)
        adapter = CategoryAdapter(categoryTotal)
        listViewCat.adapter = adapter

    }

    private fun fillCategoryMap(){
        depenseListCopy.forEach{
            if(!(categoryTotal.containsKey(it.depCategory))){
                categoryTotal[it.depCategory] = it.amountPaid
            }
            else{
                categoryTotal[it.depCategory] = it.amountPaid + categoryTotal[it.depCategory]!!
            }
        }
    }

    //Deserializer de la liste pour en faire une copie
    private fun participantListToString(list: ArrayList<Depense>): String {
        val type = object : TypeToken<ArrayList<Depense>>() {}.type
        return Gson().toJson(list, type)
    }
    //Reserializer de la liste pour copie profonde
    private fun toParticipantList(string: String): ArrayList<Depense> {
        val itemType = object : TypeToken<ArrayList<Depense>>() {}.type
        return Gson().fromJson<ArrayList<Depense>>(string, itemType)
    }
}