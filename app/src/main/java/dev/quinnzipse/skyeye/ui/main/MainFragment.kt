package dev.quinnzipse.skyeye.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.Plane
import dev.quinnzipse.skyeye.models.planeFactory
import dev.quinnzipse.skyeye.network.OpenSkyDAO
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.main_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log

class MainFragment : Fragment() {

    private lateinit var planeAdapter: NearbyRecyclerAdapter
    private lateinit var fusedLocClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView(view)
        requestPermissions()
        var latitude = 0F
        var longitude = 0F

        if (hasLocationPermission()) {
            Log.d("LOCATION", "HAS PERMISSION!")
            fusedLocClient.lastLocation.addOnSuccessListener(this.requireActivity()) {
                Log.d("LOCATION", "Accuracy: ${it.accuracy}")
                Log.d("LOCATION", "Lat: ${it.latitude} Lon: ${it.longitude}")
                Log.d("LOCATION", "Provider: ${it.provider}")

                latitude = it.latitude.toFloat()
                longitude = it.longitude.toFloat()
            }
        }

        Log.d("LOCATION", "Latitude $latitude")
        Log.d("LOCATION", "Longitude $longitude")

        getCurrentData(ceil(latitude), ceil(longitude), floor(latitude), floor(latitude))
    }

    private fun hasLocationPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        if (hasLocationPermission()) {
            return
        }

        val permissionList = mutableListOf<String>()
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this.requireActivity(), permissionList.toTypedArray(), 0)
    }

    private fun getCurrentData(latMin: Float, lonMin: Float, latMax: Float, lonMax: Float) {
        val BASE_URL = "https://opensky-network.org/"
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenSkyDAO::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Set the init UI state.
                noPlanes.visibility = View.GONE
                progressBar.visibility = View.VISIBLE

                val response = api.getNearbyPlanes(latMin, lonMin, latMax, lonMax).execute()
                Log.d("API", "Looking for the Planes!")

                if (response.isSuccessful) {
                    Log.d("API", "Got the Planes!")
                    val data = response.body()
                    var planes: List<Plane> = ArrayList()

                    if (planes.isNotEmpty()) {
                        // create a list of planes from the response.
                        withContext(Dispatchers.Default) {
                            planes = planeFactory(data)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE

                        if (planes.isEmpty()) {
                            noPlanes.visibility = View.VISIBLE
                        } else {
                            planeAdapter.submitList(planes)
                        }

                    }

                } else {
                    Log.d("Q_API", response.errorBody().string())
                    Log.d("Q_API", response.raw().request().url().toString())
                }

            } catch (e: Exception) {
                Log.d("Q_API", e.javaClass.canonicalName.toString())
            }
        }
    }

    private fun initRecyclerView(view: View) {
        view.rvPlanes.apply {
            layoutManager = LinearLayoutManager(context)
            planeAdapter = NearbyRecyclerAdapter()
            adapter = planeAdapter
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