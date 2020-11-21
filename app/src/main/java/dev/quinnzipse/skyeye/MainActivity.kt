package dev.quinnzipse.skyeye

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.quinnzipse.skyeye.ui.main.GeofencedPlacesFragment
import dev.quinnzipse.skyeye.ui.main.MainFragment
import dev.quinnzipse.skyeye.ui.main.PlaneInfoFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNav.setOnNavigationItemSelectedListener(navItemListener)
        bottomNav.selectedItemId = R.id.nearby
    }

    private val navItemListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.flight_info -> {
                Log.d("QB_NAV", "Flight Info Selected!")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, PlaneInfoFragment())
                    .commitNow()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nearby -> {
                Log.d("QB_NAV", "Nearby Selected!")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment())
                    .commitNow()
                return@OnNavigationItemSelectedListener true
            }
            R.id.favorites -> {
                Log.d("QB_NAV", "Favorites Selected!")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, GeofencedPlacesFragment())
                    .commitNow()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}