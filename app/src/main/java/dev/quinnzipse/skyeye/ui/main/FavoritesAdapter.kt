package dev.quinnzipse.skyeye.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.models.Favorite
import kotlinx.android.synthetic.main.favorites_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FavoritesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<Favorite> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FavoritesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.favorites_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FavoritesViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun clear() {
        items = ArrayList()
        notifyDataSetChanged()
    }

    fun submitList(favorites: List<Favorite>) {
        items = favorites
        Log.d("FAVORITES_ADAPTER", "List Received: ${favorites.size}")
        notifyDataSetChanged()
    }

    class FavoritesViewHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val planeName: TextView = itemView.plane_name
        private val found: TextView = itemView.found
        private val desc: TextView = itemView.desc
        private val type: TextView = itemView.type

        fun bind(favorite: Favorite) {
            if (favorite.icao.isNotBlank()) planeName.text = favorite.icao.trim()
            found.text = SimpleDateFormat.getDateTimeInstance()
                .format(Date(favorite.lastSeen * 1000))

            val textString = "(${favorite.description})"
            desc.text = textString
            val typeString = "${favorite.manufacturer} ${favorite.type}"
            type.text = typeString
        }

    }

}