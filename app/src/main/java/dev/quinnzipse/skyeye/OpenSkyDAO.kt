package dev.quinnzipse.skyeye

import androidx.room.Dao
import dev.quinnzipse.skyeye.models.Plane
import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.GET

@Dao
interface OpenSkyDAO {
    @GET("api/states/all?")
    fun getNearbyPlanes(
        @Query("lamin") latitudeMin: Int,
        @Query("lomin") longitudeMin: Int,
        @Query("lamax") latitudeMax: Int,
        @Query("lomax") longitudeMax: Int
    ): Call<Plane>

}