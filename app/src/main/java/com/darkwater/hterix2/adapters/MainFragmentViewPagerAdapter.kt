package com.darkwater.hterix2.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.darkwater.hterix2.fragments.subFragments.AdvertisementSettingsFragment
import com.darkwater.hterix2.fragments.subFragments.RunFragment

class MainFragmentViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RunFragment()
            1 -> AdvertisementSettingsFragment()
            else -> Fragment()
        }
    }

}