package dev.quinnzipse.skyeye.services

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import dev.quinnzipse.skyeye.models.Favorite

@Dao
interface FavoritesDAO {
    @Query("SELECT * FROM favorite")
    fun getAll(): List<Favorite>

    @Query("SELECT * FROM favorite WHERE faFlightID IN (:ids)")
    fun loadAllByIds(ids: List<String>): List<Favorite>

    @Query("SELECT * FROM favorite WHERE faFlightID = :id")
    fun getByID(id: String): Favorite

    @Insert
    fun insertAll(vararg favorite: Favorite)

    @Delete
    fun delete(favorite: Favorite): Int
}