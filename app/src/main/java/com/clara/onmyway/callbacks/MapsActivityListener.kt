package com.clara.onmyway.callbacks

import com.clara.onmyway.data.Location

interface MapsActivityListener {

    fun onLocationUpdated(location : Location)

    fun onUserOffline()
}