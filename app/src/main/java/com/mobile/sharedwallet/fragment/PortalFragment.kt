package com.mobile.sharedwallet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mobile.sharedwallet.R
import com.mobile.sharedwallet.adapter.PortalAdapter

class PortalFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.portal_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager2>(R.id.portalViewPager)
        viewPager.adapter = PortalAdapter(this)

        val tabLayout = view.findViewById<TabLayout>(R.id.portalTabLayout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    // tab.text = getString(R.string.log_in)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_login_24, null)
                }
                1 -> {
                    // tab.text = getString(R.string.register)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_create_24, null)
                }
            }
        }.attach()

    }
}