package dev.quinnzipse.skyeye.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object LocationService {
    fun hasLocationPermission(context: Context) =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermissions(context: Context, activity: Activity) {
        if (hasLocationPermission(context)) {
            return
        }

        val permissionList = mutableListOf<String>()
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), 0)
    }
}