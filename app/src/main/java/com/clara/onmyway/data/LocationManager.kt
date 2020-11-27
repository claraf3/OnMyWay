package com.clara.onmyway.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest

import java.util.concurrent.TimeUnit

class LocationManager(private val context : Context) {

    private val fusedLocationClient = FusedLocationProviderClient(context)

    private val _updatingLocation : MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val updatingLocation: LiveData<Boolean>
        get() = _updatingLocation

    private val pendingIntent : PendingIntent by lazy {
        val intent = Intent(context, LocationUpdateReceiver::class.java)
        intent.action = LocationUpdateReceiver.ACTION_UPDATE_LOCATION
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun startLocationUpdates() {

//        Log.d("map", "entered LocationManager.getLocationUpdates()")
        val locationRequest : LocationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(10)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime=TimeUnit.SECONDS.toMillis(15)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        try {
//            Log.d("map", "entered try request location updates")
            fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
            _updatingLocation.value = true

        } catch (noPermission : SecurityException) {
            throw noPermission
        }
    }

    fun stopLocationUpdates() {
        Log.d("map", "entered LocationManager.stopLocationUpdates()")
        fusedLocationClient.removeLocationUpdates(pendingIntent)
        _updatingLocation.value = false

    }


}