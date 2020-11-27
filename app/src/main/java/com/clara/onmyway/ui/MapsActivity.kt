package com.clara.onmyway.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.clara.onmyway.R
import com.clara.onmyway.callbacks.MapsActivityListener
import com.clara.onmyway.data.Location
import com.clara.onmyway.data.repo.FirebaseRepoImpl

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapsActivityListener {

    private lateinit var mMap: GoogleMap
    private var mMapMarker : Marker? = null

    private var lat : Double = 0.0
    private var lng : Double = 0.0

    private val repo = FirebaseRepoImpl()

    private lateinit var username : String
    private lateinit var firebaseRef : DatabaseReference
    private lateinit var valueEventListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(map_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if(intent != null && intent.hasExtra("username")) {
            username = intent!!.getStringExtra("username").toString()
            firebaseRef = Firebase.database.reference.child("Users/$username/location")
        }

//        val obsString = resources.getString(R.string.observing)
        titleView.text = String.format(resources.getString(R.string.observing), username)

        if(!username.isEmpty()) {

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //calls repo to start listening to friend location after map is initiated
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location = LatLng(lat, lng)
        val _icon = fromVectorToBitmap(R.drawable.location_icon)

        val markerOptions = MarkerOptions()
        markerOptions.apply {
            position(location)
            title(username)
            visible(true)
            icon(_icon)
        }
        mMapMarker = mMap.addMarker(markerOptions)
        val zoomLevel = 14.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        //start observing user location after map is created. Stores listener for removal.
        valueEventListener = startLocationListener()
        registerObserver(username)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        map_toolbar.setNavigationIcon(R.drawable.back_icon)
        map_toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }
        menuInflater.inflate(R.menu.empty_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //helper function to convert vector to bitmap
    private fun fromVectorToBitmap(drawable : Int) : BitmapDescriptor {

        val myDrawable = ContextCompat.getDrawable(this, drawable)


            val bitmap = Bitmap.createBitmap(myDrawable!!.intrinsicWidth, myDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            myDrawable.setBounds(0, 0, canvas.width, canvas.height)
            DrawableCompat.setTint(myDrawable, resources.getColor(R.color.black))
            myDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    //calls repo to start firebase listener for location updates
    fun startLocationListener() : ValueEventListener {

        return repo.getLocation(this, firebaseRef)
    }

    //removes observer status and removes listener
    override fun onDestroy() {
        repo.removeObserver(username)
        firebaseRef.removeEventListener(valueEventListener)
        super.onDestroy()
    }

    //log this user on firebase as a location observer of the user who is being observed
    fun registerObserver(username : String) {
        repo.registerObserver(username)
    }

    fun removeObserver(username: String) {
        repo.removeObserver(username)
    }

    //notify user that the friend is no longer sharing location
    fun alertOffline() {

        val view = findViewById<View>(R.id.map_view)
        Snackbar.make(view,
            R.string.sharing_turned_off, Snackbar.LENGTH_LONG)
            .setAction(R.string.confirm) {
                it.visibility = View.GONE
            }
            .show()
    }

    //callback to update map marker when location has updated
    override fun onLocationUpdated(location : Location) {

        if (mMapMarker != null) {
            lat = location.lat
            lng = location.lng
            val latLng = LatLng(lat, lng)
            mMapMarker!!.position = latLng

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    //callback to notify user when friend is no longer sharing location
    override fun onUserOffline() {
        alertOffline()
    }



}