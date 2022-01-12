package com.mobile.sharedwallet.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.adapter.EquilibrationAdapter
import com.mobile.sharedwallet.models.Participant
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.min
import com.github.mikephil.charting.components.LimitLine




class BalanceFragment: Fragment() {

    private var participantListCopy : ArrayList<Participant> = ArrayList()
    private lateinit var adapter : EquilibrationAdapter

    //Classe pour formatter l'axe X avec le noms des participants
    class ChartXAxisFormatter() : ValueFormatter(), Parcelable {

        private var names = arrayOf("Julien", "Robin", "Remi")

        constructor(a:ArrayList<String>) :this(){
            names = a.toTypedArray()
        }

        constructor(parcel: Parcel) : this() {
            names = parcel.createStringArray() as Array<String>
        }

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return names.getOrNull(value.toInt()) ?: value.toString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeStringArray(names)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ChartXAxisFormatter> {
            override fun createFromParcel(parcel: Parcel): ChartXAxisFormatter {
                return ChartXAxisFormatter(parcel)
            }

            override fun newArray(size: Int): Array<ChartXAxisFormatter?> {
                return arrayOfNulls(size)
            }
        }
    }

    //Classe pour formatter l'axe Y
    class ChartYValueFormatter : ValueFormatter() {

        private val format = DecimalFormat("###.#")

        // override this for BarChart
        override fun getBarLabel(barEntry: BarEntry?): String {
            return format.format(barEntry?.y)
        }

        // override this for custom formatting of XAxis or YAxis labels
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return format.format(value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = EquilibrationAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.balance_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        participantListCopy = toParticipantList(participantListToString(Shared.pot.participants))
        displayGraph()
        quiDoitQuoi()
        view.findViewById<ListView>(R.id.remboursement).adapter = adapter
    }

    private fun quiDoitQuoi(){
        //on trie la liste des participants par ordre croissant de cout
        participantListCopy.sortByDescending { it.solde }

        // on retire a toute la liste des participants le montant moyen
        var i = 0;
        var j = participantListCopy.size - 1;
        var debt : Float

        while (i < j) {
            debt = min(
                abs(participantListCopy[i].solde),
                abs(participantListCopy[j].solde)
            )

            participantListCopy[i].solde = participantListCopy[i].solde - debt;
            participantListCopy[j].solde = participantListCopy[j].solde + debt;

            adapter.add(arrayListOf(participantListCopy[j].uid, participantListCopy[i].uid, debt.toString()))
            if (participantListCopy[i].solde == 0f) {
                i++;
            }
            if (participantListCopy[j].solde == 0f) {
                j--;
            }
        }
    }

    //Recupere les valeurs pour le graph
    private fun gatherInfo() : ArrayList<BarEntry>{
        val entries: ArrayList<BarEntry> = ArrayList()
        var i = 0f
        participantListCopy?.forEach{
            entries.add(BarEntry(i, it.solde))
            i += 1.0f
        }
        return entries
    }

    //Affiche le graph avec ses proprietes
    private fun displayGraph(){
        val entries = gatherInfo()
        var names: ArrayList<String> = ArrayList()
        participantListCopy?.forEach {
            names.add("→   " + it.name + ", " +it.solde.toString())
        }

        var axeAbs = ChartXAxisFormatter(names)
        var axeOrd = ChartYValueFormatter()
        val ll1 = LimitLine(0f, "")
        ll1.lineWidth = 2f;

        val barDataSet = BarDataSet(entries, "Graph of the costs")
        barDataSet.setColors(*ColorTemplate.PASTEL_COLORS)

        var barChart = view?.findViewById<HorizontalBarChart>(R.id.barChart)
        val data = BarData(barDataSet)
        data.barWidth = 0.3f
        data.setValueTextSize(10.0f)
        if (barChart != null) {
            barChart.data = data

            // affiche le repere du graph toutes les dizaines
            barChart.axisLeft.setDrawGridLines(false)
            //trace un trait d'axe au milieu de chaque entree
            barChart.xAxis.setDrawGridLines(true)
            //
            barChart.xAxis.setDrawAxisLine(false)
            barChart.xAxis.granularity = 1.0f
            barChart.xAxis.valueFormatter = axeAbs
            barChart.axisLeft.valueFormatter = axeOrd
            //taille du texte des participants
            barChart.xAxis.textSize = 15.0f
            barChart.axisLeft.textSize = 8.0f
            barChart.axisLeft.addLimitLine(ll1)
            //Inverser la position labels/graph
            //barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

            //axe y de droite
            barChart.axisRight.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.description.isEnabled = false
            barChart.setDrawValueAboveBar(true)

            //duree de l'anim
            barChart.animateY(500)
            barChart.invalidate()
        }
        else {
            println("barchart not found !")
        }
    }
    //Deserializer de la liste pour en faire une copie
    private fun participantListToString(list: ArrayList<Participant>): String {
        val type = object : TypeToken<ArrayList<Participant>>() {}.type
        return Gson().toJson(list, type)
    }
    //Reserializer de la liste pour copie profonde
    private fun toParticipantList(string: String): ArrayList<Participant> {
        val itemType = object : TypeToken<ArrayList<Participant>>() {}.type
        return Gson().fromJson<ArrayList<Participant>>(string, itemType)
    }
}