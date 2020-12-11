package dev.quinnzipse.skyeye.ui.main

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.room.Database
import androidx.room.RoomDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.AircraftType
import dev.quinnzipse.skyeye.models.AircraftTypeResult
import dev.quinnzipse.skyeye.models.Favorite
import dev.quinnzipse.skyeye.models.InFlightInfoResult
import dev.quinnzipse.skyeye.network.FlightAware
import dev.quinnzipse.skyeye.network.OpenSkyDAO
import dev.quinnzipse.skyeye.services.AppDatabase
import dev.quinnzipse.skyeye.services.LocationService
import kotlinx.android.synthetic.main.fragment_plane_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class PlaneInfoFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocClient: FusedLocationProviderClient
    lateinit var map: GoogleMap
    lateinit var mv: MapView
    lateinit var api: OpenSkyDAO
    private val BASE_URL: String = "https://opensky-network.org/"
    private val threshold: Float = 1.5F
    private val refreshTime: Long = 5000
    private var cancel: Boolean = false
    private val markers: ArrayList<Marker> = ArrayList()
    private val paths: ArrayList<Polyline> = ArrayList()
    private lateinit var key: String
    private lateinit var userName: String
    private lateinit var currentAircraftType: AircraftTypeResult
    private lateinit var currentAircraft: InFlightInfoResult
    private var isFavorited: Boolean = false
    private lateinit var star: Drawable
    private lateinit var favorite: Drawable
    private lateinit var database: AppDatabase

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

        key = resources.getString(R.string.a)
        userName = resources.getString(R.string.u)
        star = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_star_border_24, null)!!
        favorite = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_star_24, null)!!
        GlobalScope.launch(Dispatchers.Default) {
            database = AppDatabase.invoke(context!!)
        }

        return v
    }

    private fun fabClick(view: View) {
        GlobalScope.launch(Dispatchers.IO) {
            Log.d("FAB", currentAircraft.ident)

            if (isFavorited) {
                favoriteFAB.setImageDrawable(star)

                database.FavoritesDAO().getByID(currentAircraft.faFlightID).also {
                    if (it != null) {
                        val rowsRemoved = database.FavoritesDAO().delete(it)
                        Log.d("DATABASE", "Removed: $rowsRemoved")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context!!,
                                "Removed ${currentAircraft.ident} from Favorites!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context!!,
                                "${currentAircraft.ident} wasn't found in Favorites!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                favoriteFAB.setImageDrawable(favorite)


                try {
                    database.FavoritesDAO().insertAll(
                        Favorite(
                            currentAircraft.faFlightID,
                            currentAircraft.ident,
                            currentAircraftType.type,
                            currentAircraftType.manufacturer,
                            currentAircraftType.description,
                            Date().time / 1000
                        )
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context!!,
                            "Added ${currentAircraft.ident} to Favorites!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: SQLiteConstraintException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context!!,
                            "${currentAircraft.ident} already exists!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            isFavorited = !isFavorited

            val list = database.FavoritesDAO().getAll()
            list.forEach {
                Log.d("DATABASE", it.icao)
            }


        }
    }

    private fun fabShow() {
        GlobalScope.launch(Dispatchers.IO) {
            database.FavoritesDAO().getByID(currentAircraft.faFlightID).also {
                if (it == null) {
                    isFavorited = false
                    withContext(Dispatchers.Main) {
                        favoriteFAB.setImageDrawable(star)
                    }
                } else {
                    isFavorited = true
                    withContext(Dispatchers.Main) {
                        favoriteFAB.setImageDrawable(favorite)
                    }
                }
            }
        }

        favoriteFAB.visibility = View.VISIBLE
    }

    private fun fabHide() {
        isFavorited = false
        favoriteFAB.visibility = View.GONE
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
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        map.moveCamera(CameraUpdateFactory.zoomTo(8.9F))

        keepUpdated()
        fusedLocClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener {
                val latitude = it.latitude.toFloat()
                val longitude = it.longitude.toFloat()

                map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                Log.d("MAP", "$latitude, $longitude")
            }


        map.setOnMarkerClickListener {
            paths.forEach { path ->
                path.remove()
            }

            getFlightInfo(it)
            false
        }

        favoriteFAB.setOnClickListener {
            fabClick(it)
        }

    }

    private fun getFlightInfo(marker: Marker) {
        val icao = marker.title.trim()
        Log.d("FLIGHT_INFO", "Getting Flight Info for: $icao")
        GlobalScope.launch(Dispatchers.IO) {
            val url = "http://flightxml.flightaware.com/json/FlightXML2/"
            val api = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FlightAware::class.java)

            val response = api.getInFlightInfo(icao).execute()
            Log.d("FLIGHT_INFO", response.raw().request().url().toString())

            if (response.isSuccessful) {
                Log.d("FLIGHT_INFO", "Successful!!!")
                currentAircraft = response.body().InFlightInfoResult

                val typeRes = api.AircraftType(currentAircraft.type).execute()

                val waypoints: ArrayList<LatLng> = ArrayList()
                withContext(Dispatchers.Default) {
                    val scanner = Scanner(currentAircraft.waypoints)
                    val i = 0
                    while (scanner.hasNextDouble()) {
                        // Take only half of the waypoints.
                        if (i % 2 == 0) {
                            waypoints.add(LatLng(scanner.nextDouble(), scanner.nextDouble()))
                        }
                    }
                }

                if (typeRes.isSuccessful) {
                    currentAircraftType = typeRes.body().AircraftTypeResult

                    withContext(Dispatchers.Main) {
                        details.visibility = View.VISIBLE
                        fabShow()

                        flightName.text = currentAircraft.ident

                        origin.text =
                            if (currentAircraft.origin.isBlank()) "N/A"
                            else currentAircraft.origin.substring(1)
                        destination.text =
                            if (currentAircraft.destination.isBlank()) "N/A"
                            else currentAircraft.destination.substring(1)

                        val typeText =
                            "${currentAircraftType.manufacturer} ${currentAircraftType.type}"
                        type.text = typeText
                        eta.text = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                            .format(Date(currentAircraft.departureTime.toLong() * 1000))

                        paths.add(
                            map.addPolyline(
                                PolylineOptions().addAll(waypoints).width(4F).geodesic(true)
                                    .color(0x7f5BC0BE)
                            )
                        )

                        map.setOnMapClickListener {
                            details.visibility = View.GONE
                            fabHide()
                            paths.forEach {
                                it.remove()
                            }
                        }
                    }

                }

            } else {
                Log.d("FLIGHT_INFO", response.errorBody().string())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshMap() {

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
                    withContext(Dispatchers.Main) {
                        markers.forEach { marker ->
                            marker.remove()
                        }
                    }

                    val planes = response.body().states
                    if (!planes.isNullOrEmpty()) {

                        // Generate the plane markers!
                        withContext(Dispatchers.Main) {
                            for (plane in planes) {
                                markers.add(addPlaneMarker(plane))
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
        val alt = if (plane[7] != null) plane[7] as Double else 0.0
        val bearing = plane[10] as Double

        call = if (call.isBlank()) "Unknown"; else call

        return map.addMarker(
            MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(LatLng(lat, lon))
                .visible(true)
                .title(call)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.raw.plane_icon)
                )
                .rotation(bearing.toFloat() + 90)
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