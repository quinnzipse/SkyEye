package dev.quinnzipse.skyeye.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Favorite(
    @PrimaryKey
    var faFlightID: String,
    var icao: String,
    var type: String,
    var manufacturer: String,
    var description: String,
    var lastSeen: Long
)
