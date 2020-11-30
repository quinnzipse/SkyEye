package dev.quinnzipse.skyeye.ui.main

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MainFragment : Fragment() {

    private lateinit var planeAdapter: NearbyRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView(view)
        getCurrentData(43.5F, -93F, 44.5F, -92F)
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
                progressBar.visibility = View.VISIBLE
                val response = api.getNearbyPlanes(latMin, lonMin, latMax, lonMax).execute()
                Log.d("API", "Looking for the Planes!")

                if (response.isSuccessful) {
                    Log.d("API", "Got the Planes!")
                    val data = response.body()
                    var planes: List<Plane> = ArrayList()

                    withContext(Dispatchers.Default) {
                        planes = planeFactory(data)
                    }

                    withContext(Dispatchers.Main) {
                        planeAdapter.submitList(planes)
                        progressBar.visibility = View.GONE
                    }

                } else {
                    Log.d("Q_API", response.errorBody().string())
                    Log.d("Q_API", response.raw().request().url().toString())
                }

            } catch (e: Exception) {
                Log.d("Q_API", e.message.toString())
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

}