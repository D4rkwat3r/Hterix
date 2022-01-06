package com.darkwater.hterix2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.darkwater.hterix2.R
import com.darkwater.hterix2.adapters.MainFragmentViewPagerAdapter
import com.darkwater.hterix2.http.Telemetry
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {

    lateinit var content: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        content =  inflater.inflate(R.layout.fragment_main, container, false)
        onReady(content)
        return content
    }

    private fun onReady(view: View) {
        val tabs = view.findViewById<TabLayout>(R.id.tabMenu)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = MainFragmentViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.view.isClickable = true
                    tab.text = "Запуск"
                }
                1 -> {
                    tab.view.isClickable = true
                    tab.text = "Настройки"
                }
            }
        }.attach()
    }

}