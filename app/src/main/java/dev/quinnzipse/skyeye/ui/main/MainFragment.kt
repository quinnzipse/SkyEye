package dev.quinnzipse.skyeye.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.Plane
import dev.quinnzipse.skyeye.models.planeFactory
import dev.quinnzipse.skyeye.network.OpenSkyDAO
import kotlinx.android.synthetic.main.fragment_plane_info.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.main_fragment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainFragment : Fragment() {

    private lateinit var planeAdapter: NearbyRecyclerAdapter
    private lateinit var fusedLocClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private val threshold: Float = .4F
    private val refreshTime: Long = 12000
    private var cancel: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        fusedLocClient = LocationServices.getFusedLocationProviderClient(view.context)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView(view)
        refreshRV()

        keepUpdated()

        swipeLayout.setOnRefreshListener {
            refreshRV(false)
        }
    }

    private fun keepUpdated() {
        Handler().postDelayed(
            {
                if (!cancel) {
                    refreshRV(false)
                    keepUpdated()
                }
            },
            refreshTime
        )
    }

    @Override
    override fun onPause() {
        super.onPause()
        cancel = true
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        cancel = true
    }

    @SuppressLint("MissingPermission")
    private fun refreshRV(showLoader: Boolean = true) {
        requestPermissions()

        if (hasLocationPermission(requireContext())) {
            Log.d("LOCATION", "HAS PERMISSION!")

            fusedLocClient.lastLocation.addOnSuccessListener {

                val latitude = it.latitude.toFloat()
                val longitude = it.longitude.toFloat()

                getCurrentData(
                    latitude - threshold,
                    longitude - threshold,
                    latitude + threshold,
                    longitude + threshold,
                    showLoader
                )
            }
        } else {
            val list: ArrayList<String> = ArrayList()
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
                AppSettingsDialog.Builder(this).build().show()
            }
        }
    }

    private fun hasLocationPermission(context: Context) =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        if (hasLocationPermission(requireContext())) {
            return
        }

        val permissionList = mutableListOf<String>()
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(requireActivity(), permissionList.toTypedArray(), 0)
    }

    private fun getCurrentData(
        latMin: Float,
        lonMin: Float,
        latMax: Float,
        lonMax: Float,
        showLoader: Boolean
    ) {
        val BASE_URL = "https://opensky-network.org/"
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenSkyDAO::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Set the init UI state.
                withContext(Dispatchers.Main) {
                    if (showLoader) {
                        noPlanes.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                    }
                }

                val response = api.getNearbyPlanes(latMin, lonMin, latMax, lonMax).execute()

                if (response.isSuccessful) {
                    val data = response.body()
                    var planes: List<Plane> = ArrayList()

                    if (data != null && !data.states.isNullOrEmpty()) {
                        // create a list of planes from the response.
                        withContext(Dispatchers.Default) {
                            planes = planeFactory(data)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        swipeLayout.isRefreshing = false

                        if (planes.isEmpty()) {
                            planeAdapter.clear()
                            noPlanes.visibility = View.VISIBLE
                        } else {
                            noPlanes.visibility = View.GONE
                            planeAdapter.submitList(planes)
                        }
                    }
                } else {
                    Log.d("Q_API", response.errorBody().string())
                    Log.d("Q_API", response.raw().request().url().toString())
                }

            } catch (e: Exception) {
                Log.d("Q_API", e.stackTraceToString())
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