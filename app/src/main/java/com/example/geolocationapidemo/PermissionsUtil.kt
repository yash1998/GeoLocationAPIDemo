package com.example.geolocationapidemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

object PermissionsUtil {

    const val START_SERVICE = "start_service"
    const val STOP_SERVICE = "stop_service"

    fun askingPermissions(
        activityInstance: AppCompatActivity,
        permissions: Array<String>,
        onSuccess: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activityInstance.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                onSuccess()
            } else {
                activityInstance.requestPermissions(permissions, 101)
            }
        }
    }

    fun getAddress(context: Context, lat: Double?, lng: Double?): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var address = ""
        try {
            val addresses: List<Address> = geocoder.getFromLocation(lat ?: 0.0, lng ?: 0.0, 1)
            val obj: Address = addresses[0]
            address = obj.subLocality + ", " + obj.adminArea + ", " + obj.countryName
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return address
    }

}