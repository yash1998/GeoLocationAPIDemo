package com.example.geolocationapidemo

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.geolocationapidemo.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var binding: ActivityMainBinding? = null
    var client: FusedLocationProviderClient? = null
    var map: GoogleMap? = null
    var locationRequest: LocationRequest? = null
    val LOCATION_UPDATE_INTERVAL = 30 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        client = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding?.fetchLastLocationButton?.setOnClickListener { fetchLastLocation() }
        binding?.requestLocationUpdateButton?.setOnClickListener { requestLocationUpdate() }
        binding?.stopLocationUpdateButton?.setOnClickListener { stopLocationUpdate() }
        binding?.requestLocationUpdateBackgroundButton?.setOnClickListener { requestLocationUpdateBackground() }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.setMinZoomPreference(13f)
        map?.setMaxZoomPreference(15f)
    }

    private fun markLocationOnMap(lat: Double?, lng: Double?) {
        val lastLocation = LatLng(lat ?: 0.0, lng ?: 0.0)
        map?.clear()
        map?.addMarker(MarkerOptions().position(lastLocation).title("Current Location"))
        map?.moveCamera(CameraUpdateFactory.newLatLng(lastLocation))
    }

    private fun stopLocationUpdate() {
        client?.removeLocationUpdates(locationCallback)
        stopService(Intent(this@MainActivity, FetchLocationBackgroundService::class.java))
    }

    private fun requestLocationUpdateBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (applicationContext.checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startService(Intent(this@MainActivity, FetchLocationBackgroundService::class.java))
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun requestLocationUpdate() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = LOCATION_UPDATE_INTERVAL

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (applicationContext.checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                client?.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 101)
            }
        }
    }

    private fun fetchLastLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (applicationContext.checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //get location
                client?.lastLocation?.addOnSuccessListener(locationListener)
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 101)
            }
        }
    }

    private var locationListener = OnSuccessListener<Location> {
        markLocationOnMap(it?.latitude, it?.longitude)
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                markLocationOnMap(location?.latitude, location?.longitude)
                Toast.makeText(
                    this@MainActivity,
                    "Location Updated: " + PermissionsUtil.getAddress(this@MainActivity, location?.latitude, location?.longitude),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}