package dev.quinnzipse.skyeye.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.quinnzipse.skyeye.NearbyRecyclerAdapter
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.Plane
import dev.quinnzipse.skyeye.models.PositionSources
import kotlinx.android.synthetic.main.main_fragment.view.*

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
        addDataSet()
    }

    private fun addDataSet() {
        val list = ArrayList<Plane>()
        list.add(createDefaultPlane("Test Plane 1"))
        list.add(createDefaultPlane("Jill's Plane"))
        list.add(createDefaultPlane("Quinn's Plane"))
        list.add(createDefaultPlane("Test Plane 2"))
        planeAdapter.submitList(list)
    }

    private fun createDefaultPlane(callsign: String): Plane {
        return Plane(
            callsign, "", "", 0, 0,
            0F, 0F, 0F, false, 0F,
            0F, 0F, 0F, "",
            false, PositionSources.ADS_B
        )

    }

    private fun initRecyclerView(view: View) {
        view.rvPlanes.apply {
            layoutManager = LinearLayoutManager(context)
            planeAdapter = NearbyRecyclerAdapter()
            adapter = planeAdapter
        }
    }

}