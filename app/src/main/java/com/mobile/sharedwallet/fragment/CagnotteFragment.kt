package com.mobile.sharedwallet.fragment


import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import com.mobile.sharedwallet.MainActivity
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils

class CagnotteFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shared.cagnotteFragment = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.cagnotte_fragment, container, false)
    }


    override fun onStart() {
        super.onStart()
        Utils.checkLoggedIn(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title : Chip = view.findViewById(R.id.cagnotteName)
        title.text = Shared.pot.name
        title.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(Shared.pot.color)))

        // Set up navigation bar & navigation controller
        val navController = (childFragmentManager.findFragmentById(R.id.fragmentPlace) as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigationHome, R.id.navigationBalance, R.id.navigationCategory))
        setupActionBarWithNavController(requireActivity() as MainActivity, navController, appBarConfiguration)
        setupWithNavController(view.findViewById<BottomNavigationView>(R.id.navView), navController)
    }


    fun update(color : String? = null, name : String? = null) {
        view?.let { v : View ->
            val title : Chip = v.findViewById(R.id.cagnotteName)
            color?.let { c : String ->
                title.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(c)))
            }
            name?.let { n : String ->
                title.text = n
            }
        }
    }
}