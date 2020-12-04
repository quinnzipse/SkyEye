package dev.quinnzipse.skyeye.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mv.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mv.onResume()
    }

    override fun onPause() {
        super.onPause()
        mv.onPause()
    }

    override fun onStop() {
        super.onStop()
        mv.onStop()
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
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.moveCamera(CameraUpdateFactory.zoomTo(10F))

        fusedLocClient.lastLocation.addOnSuccessListener {
            val latitude = it.latitude.toFloat()
            val longitude = it.longitude.toFloat()

            map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            Log.d("MAP", "$latitude, $longitude")
            map.addPolygon(
                PolygonOptions().add(
                    LatLng(
                        latitude.toDouble() - threshold,
                        longitude.toDouble() + threshold
                    )
                )
                    .add(LatLng(latitude.toDouble() - threshold, longitude.toDouble() - threshold))
                    .add(LatLng(latitude.toDouble() + threshold, longitude.toDouble() - threshold))
                    .add(LatLng(latitude.toDouble() + threshold, longitude.toDouble() + threshold))
            )

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
                        Log.d("MAP", "Adding Airplanes....")
                        withContext(Dispatchers.Main) {
                            for (plane in planes) {
                                val lon = plane[5] as Double
                                val lat = plane[6] as Double
                                val call = plane[1] as String
                                val alt = plane[7] as Double
                                val velocity = plane[9] as Double

                                Log.d("MAP", "$lon, $lat, $call, $alt, $velocity")
                                map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(lat, lon))
                                        .visible(true)
                                        .title(call)
//                                        .icon(
//                                            BitmapDescriptorFactory.fromResource(R.raw.plane_icon)
//                                        )
                                        .snippet("Altitude: $alt\nVelocity: $velocity")
                                        .zIndex(4f)
                                )
                            }
                        }
                    } else {
                        Log.d("MAP", "No Airplanes to be added")
                    }
                }
            }
        }

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