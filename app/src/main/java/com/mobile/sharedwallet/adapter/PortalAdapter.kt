package com.mobile.sharedwallet.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobile.sharedwallet.fragment.LoginFragment
import com.mobile.sharedwallet.fragment.RegisterFragment

class PortalAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() : Int = 2

    override fun createFragment(position: Int) : Fragment = when (position) {
        0 -> LoginFragment()
        1 -> RegisterFragment()
        else -> LoginFragment()
    }
}