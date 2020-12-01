package dev.quinnzipse.skyeye.services

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object LocationService {
    fun hasLocationPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
}