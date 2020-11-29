package dev.quinnzipse.skyeye.network

import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.GET

interface OpenSkyDAO {
    @GET("api/states/all")
    fun getNearbyPlanes(
        @Query("lamin") latitudeMin: Int,
        @Query("lomin") longitudeMin: Int,
        @Query("lamax") latitudeMax: Int,
        @Query("lomax") longitudeMax: Int
    ): Call<StateResponse>

}