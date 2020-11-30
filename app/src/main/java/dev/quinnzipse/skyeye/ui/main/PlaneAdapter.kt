package dev.quinnzipse.skyeye.ui.main


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.Plane
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.text_row_item.view.*


class NearbyRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<Plane> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PlaneViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaneViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(planes: List<Plane>) {
        items = planes
        Log.d("PLANE_ADAPTER", "List Received: ${planes.size}")
        notifyDataSetChanged()
    }

    class PlaneViewHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val planeName: TextView = itemView.plane_name
        private val lon: TextView = itemView.lon
        private val lat: TextView = itemView.lat
        private val altitude: TextView = itemView.altitude

        fun bind(plane: Plane) {
            planeName.text = plane.callsign
            lat.text = plane.latitude.toString()
            lon.text = plane.longitude.toString()
            altitude.text = plane.barometerAltitude.toString()
        }

    }

}