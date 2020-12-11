package dev.quinnzipse.skyeye.models

data class InFlightInfoResult(
    val altitude: Int,
    val altitudeChange: String,
    val altitudeStatus: String,
    val arrivalTime: Int,
    val departureTime: Int,
    val destination: String,
    val faFlightID: String,
    val firstPositionTime: Int,
    val groundspeed: Int,
    val heading: Int,
    val highLatitude: Double,
    val highLongitude: Double,
    val ident: String,
    val latitude: Double,
    val longitude: Double,
    val lowLatitude: Double,
    val lowLongitude: Double,
    val origin: String,
    val prefix: String,
    val suffix: String,
    val timeout: String,
    val timestamp: Int,
    val type: String,
    val updateType: String,
    val waypoints: String
)