package dev.quinnzipse.skyeye

enum class PositionSources {
    ADS_B, ASTERIX, MLAT
}

data class Plane(
    val icao24: String,
    val callsign: String,
    val originCountry: String,
    val timePosition: Int?,
    val lastContact: Int?,
    val longitude: Float?,
    val latitude: Float?,
    val barometerAltitude: Float?,
    val onGround: Boolean,
    val velocity: Float?,
    val trueTrack: Float?,
    val verticalRate: Float?,
    val geometricAltitude: Float?,
    val sqawk: String?,
    val spi: Boolean,
    val positionSource: PositionSources
)