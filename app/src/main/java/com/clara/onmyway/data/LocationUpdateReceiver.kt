package com.clara.onmyway.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.android.gms.location.LocationResult

class LocationUpdateReceiver : BroadcastReceiver() {

    private val repo = FirebaseRepoImpl()

    //stores newly received location coordiantes onto firebase
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("map", "entered LocationUpdateReceiver onReceive())")

        if(intent.action == ACTION_UPDATE_LOCATION) {
//            Log.d("map", "entered intent.action == ACTION_UPDATE_LOCATION")
            LocationResult.extractResult(intent).let { locationResult ->
                if(locationResult != null) {
                    val location = locationResult.lastLocation
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("map", "lat is $lat, long is $lng")

                    val userLoc =
                        Location(lat, lng, true)
                    repo.updateLocation(userLoc)
                }

            }
        }
    }

    companion object {
        const val ACTION_UPDATE_LOCATION = "UPDATE_LOCATION"
    }


}