package dev.quinnzipse.skyeye.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import dev.quinnzipse.skyeye.R
import kotlinx.android.synthetic.main.fragment_plane_info.*

class PlaneInfoFragment : Fragment(), OnMapReadyCallback {

    lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_plane_info, container, false)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    @SuppressLint("MissingPermission")
    @Override
    override fun onMapReady(gMap: GoogleMap) {
        map = gMap
//        requestPermissions()
//        map.isMyLocationEnabled = true
//        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(40.3, 40.5)))
    }

}