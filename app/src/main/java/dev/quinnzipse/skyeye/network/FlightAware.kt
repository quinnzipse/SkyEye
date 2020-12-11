package dev.quinnzipse.skyeye.network

import dev.quinnzipse.skyeye.models.AircraftType
import dev.quinnzipse.skyeye.models.InFlightInfo
import dev.quinnzipse.skyeye.models.StateResponse
import retrofit2.Call
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.Headers

interface FlightAware {
    @Headers("Authorization: Basic cXppcHNlOmY0NTNkMjliZjI0NTVmODczNzc1ZjI4MTdhYjU3NWFjZmVmZjc3Y2Y=")
    @GET("InFlightInfo")
    fun getInFlightInfo(
        @Query("ident") ICAO: String
    ): Call<InFlightInfo>

    @Headers("Authorization: Basic cXppcHNlOmY0NTNkMjliZjI0NTVmODczNzc1ZjI4MTdhYjU3NWFjZmVmZjc3Y2Y=")
    @GET("AircraftType")
    fun AircraftType(
        @Query("type") type: String
    ): Call<AircraftType>

}