package dev.quinnzipse.skyeye.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.quinnzipse.skyeye.R
import dev.quinnzipse.skyeye.services.AppDatabase
import kotlinx.android.synthetic.main.fragment_favorited_places_fragement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteFragment : Fragment() {

    private lateinit var favoriteAdapter: FavoritesAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_favorited_places_fragement, container, false)

        GlobalScope.launch(Dispatchers.IO) {
            database = AppDatabase.invoke(context!!)

            val list = database.FavoritesDAO().getAll()
            if (list.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    favoriteAdapter.clear()
                    noFavorites.visibility = View.VISIBLE
                }
            } else {
                withContext(Dispatchers.Main) {
                    noFavorites.visibility = View.GONE
                    favoriteAdapter.submitList(list)
                }
            }
        }

        return v

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritesList.apply {
            layoutManager = LinearLayoutManager(context!!)
            favoriteAdapter = FavoritesAdapter()
            adapter = favoriteAdapter
        }

        clearButton.setOnClickListener {

        }
    }

}