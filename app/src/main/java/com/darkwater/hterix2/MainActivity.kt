package com.darkwater.hterix2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.darkwater.hterix2.fragments.CommunitiesFragment
import com.darkwater.hterix2.fragments.MainFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.app_nav)
        val mainFragment = MainFragment()
        val communitiesFragment = CommunitiesFragment()
        replaceFragment(mainFragment)
        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_main -> replaceFragment(mainFragment)
                R.id.nav_community -> replaceFragment(communitiesFragment)
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitNow()
    }

    override fun onBackPressed() {}
}