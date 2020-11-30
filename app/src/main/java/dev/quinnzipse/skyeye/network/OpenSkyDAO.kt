package dev.quinnzipse.skyeye.network

import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.GET

interface OpenSkyDAO {
    @GET("api/states/all")
    fun getNearbyPlanes(
        @Query("lamin") latitudeMin: Float,
        @Query("lomin") longitudeMin: Float,
        @Query("lamax") latitudeMax: Float,
        @Query("lomax") longitudeMax: Float
    ): Call<StateResponse>

}