package dev.quinnzipse.skyeye.models

import android.util.Log
import dev.quinnzipse.skyeye.network.StateResponse

enum class PositionSources {
    ADS_B, ASTERIX, MLAT, UNKNOWN
}

data class Plane(
    val icao24: String,
    val callsign: String,
    val originCountry: String,
    val timePosition: Double?,
    val lastContact: Double?,
    val longitude: Double?,
//    val longitude: Float?,
    val latitude: Double?,
//    val latitude: Float?,
    val barometerAltitude: Double?,
    val onGround: Boolean,
    val velocity: Double?,
    val trueTrack: Double?,
    val verticalRate: Double?,
    val geometricAltitude: Double?,
    val sqawk: String?,
    val spi: Boolean,
    val positionSource: PositionSources
)

fun planeFactory(data: StateResponse): List<Plane> {
    val list: ArrayList<Plane> = ArrayList()

    for (plane in data.states) {
        if (plane.size == 17) {

            Log.d("PLANE_FACTORY", plane.joinToString(", "))

            val pos: PositionSources = when (plane[16]) {
                0 -> PositionSources.ADS_B
                1 -> PositionSources.ASTERIX
                2 -> PositionSources.MLAT
                else -> PositionSources.UNKNOWN
            }

            val p = Plane(
                plane[0] as String, plane[1] as String, plane[2] as String, plane[3] as Double?,
                plane[4] as Double?, plane[5] as Double?, plane[6] as Double?, plane[7] as Double?,
                plane[8] as Boolean, plane[9] as Double?, plane[10] as Double?, plane[11] as Double?,
                plane[13] as Double?, plane[14] as String?, plane[15] as Boolean, pos
            )

            list.add(p)
        }
    }

    Log.d("PLANE_FACTORY", list.size.toString())

    return list
}