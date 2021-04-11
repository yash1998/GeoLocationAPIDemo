package com.example.geolocationapidemo

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class FetchLocationBackgroundService : Service() {

    lateinit var locationRequest: LocationRequest
    val LOCATION_UPDATE_INTERVAL = 2 * 1000L

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == PermissionsUtil.START_SERVICE) {
                startLocationUpdates()
            } else if (it.action == PermissionsUtil.STOP_SERVICE) {
                stopLocationUpdates()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                Log.d(
                    "GeoLocationAPIDemo", "Location Updated: " + PermissionsUtil.getAddress(
                        this@FetchLocationBackgroundService,
                        location?.latitude,
                        location?.longitude
                    )
                )
            }
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LOCATION_UPDATE_INTERVAL
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopSelf()
    }

}