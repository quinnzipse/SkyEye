package dev.quinnzipse.skyeye.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.network.OpenSkyDAO
import dev.quinnzipse.skyeye.services.LocationService
import kotlinx.android.synthetic.main.fragment_plane_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaneInfoFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocClient: FusedLocationProviderClient
    lateinit var map: GoogleMap
    lateinit var mv: MapView
    lateinit var api: OpenSkyDAO
    private val BASE_URL: String = "https://opensky-network.org/"
    private val threshold: Float = 1F
    private val refreshTime: Long = 10000
    private var cancel: Boolean = false
    private val markers: ArrayList<Marker> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_plane_info, container, false)

        fusedLocClient = LocationServices.getFusedLocationProviderClient(v.context)

        mv = v.findViewById(R.id.mapView)
        mv.onCreate(savedInstanceState)
        mv.getMapAsync(this)

        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenSkyDAO::class.java)

        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        mv.onDestroy()
        cancel = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mv.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        cancel = false
    }

    override fun onResume() {
        super.onResume()
        mv.onResume()
        cancel = false
    }

    override fun onPause() {
        super.onPause()
        mv.onPause()
        cancel = true
    }

    override fun onStop() {
        super.onStop()
        mv.onStop()
        cancel = true
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mv.onLowMemory()
    }


    @SuppressLint("MissingPermission")
    @Override
    override fun onMapReady(gMap: GoogleMap) {
        map = gMap
        LocationService.requestPermissions(context!!, activity!!)
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.moveCamera(CameraUpdateFactory.zoomTo(8.9F))

        keepUpdated()

//        fusedLocClient.lastLocation.addOnSuccessListener {
//            val latitude = it.latitude.toFloat()
//            val longitude = it.longitude.toFloat()
//
//            map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
//            Log.d("MAP", "$latitude, $longitude")

//            GlobalScope.launch(Dispatchers.IO) {
//                val response = api.getNearbyPlanes(
//                    latitude - threshold,
//                    longitude - threshold,
//                    latitude + threshold,
//                    longitude + threshold
//                ).execute()
//
//                if (response.isSuccessful) {
//                    val planes = response.body().states
//                    if (!planes.isNullOrEmpty()) {
//
//                        // Generate the plane markers!
//                        withContext(Dispatchers.Main) {
//                            for (plane in planes) {
//                                markers.add(addPlaneMarker(plane))
//                            }
//                        }
//
//                    }
//                }
//
//            }
//        }

    }

    @SuppressLint("MissingPermission")
    private fun refreshMap() {
        markers.forEach {
            it.remove()
        }

        LocationService.requestPermissions(context!!, activity!!)
        fusedLocClient.lastLocation.addOnSuccessListener {
            val latitude = it.latitude.toFloat()
            val longitude = it.longitude.toFloat()
            GlobalScope.launch(Dispatchers.IO) {
                val response = api.getNearbyPlanes(
                    latitude - threshold,
                    longitude - threshold,
                    latitude + threshold,
                    longitude + threshold
                ).execute()

                if (response.isSuccessful) {
                    val planes = response.body().states
                    if (!planes.isNullOrEmpty()) {

                        // Generate the plane markers!
                        withContext(Dispatchers.Main) {
                            for (plane in planes) {
                                addPlaneMarker(plane);
                            }
                        }

                    }
                }
            }
        }
    }

    private fun keepUpdated() {
        Handler().postDelayed(
            {
                if (!cancel) {
                    refreshMap()
                    keepUpdated()
                }
            },
            refreshTime
        )
    }

    private fun addPlaneMarker(plane: List<Any>): Marker {
        val lon = plane[5] as Double
        val lat = plane[6] as Double
        var call = plane[1] as String
        val alt = plane[7] as Double
        val bearing = plane[10] as Double

        call = if (call.isBlank()) "Unknown"; else call

        return map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lon))
                .visible(true)
                .title(call)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.raw.plane_icon)
                )
                .rotation(bearing.toFloat() + 90)
                .anchor(0.5f, 0.5f)
                .snippet("Alt: ${alt.toInt()}m")
                .zIndex(alt.toFloat())
                .flat(true)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}