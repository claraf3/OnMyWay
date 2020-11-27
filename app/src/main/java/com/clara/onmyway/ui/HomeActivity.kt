package com.clara.onmyway.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.clara.onmyway.R
import com.clara.onmyway.data.LocationManager
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    private lateinit var requestPermissionLauncher : ActivityResultLauncher<Array<String>>

    private lateinit var auth : FirebaseAuth
    private lateinit var authStateListener : FirebaseAuth.AuthStateListener
    private var user : FirebaseUser? = null

    private val repo = FirebaseRepoImpl()
    private lateinit var locationManager : LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar_home)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //retrieve saved state of switch button from onDestroy()
        val wasSharingLoc = getSharedPreferences("shareStatus", 0).getBoolean("isSharing", false)
        switchShare.isChecked = wasSharingLoc

        auth = FirebaseAuth.getInstance()
        locationManager = LocationManager(this)

        if(auth.currentUser != null) {
            Log.d("user", "user detected from HomeActivity")
            user = auth.currentUser!!
            titleView.text = user!!.displayName
        }

        //listens to firebase authorization status
        authStateListener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
            if(user == null) {
                Log.d("auth", "user not logged in yet")
                Intent(this, SignInActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also{ startActivity(it) }
                finish()

            } else {
                Log.d("auth", "user logged in, $user")
                Log.d("log", "user info : ${user!!.email}, ${user!!.displayName}")
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                Log.d("map", "key: ${it.key} , value: ${it.value}")
            }
        }
        //check user permissions
        if(!permissionsGranted(permissions)) {
            Log.d("map","checking initial permissions")
            requestPermissionLauncher.launch(permissions)
        }

        btnFriend.setOnClickListener {
            val intent = Intent(this, FriendListActivity::class.java)
            intent.putExtra("isSharingLoc", switchShare.isChecked)
            startActivity(intent)
        }

        btnObservers.setOnClickListener {
            val intent = Intent(this, ObserverListActivity::class.java)
            startActivity(intent)
        }

        switchShare.setOnCheckedChangeListener { buttonView, isChecked ->
//            Log.d("map", "Entered switchShare check change listener")
            if(isChecked) {
                when (permissionsGranted(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))){
                    true -> startShareLocation()
                    false -> {
                        requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                        switchShare.isChecked = false
                    }
                }
            } else if(!isChecked){
//                Log.d("map", "entered !isChecked")
                    stopShareLocation()
            }
        }
    }

    //starts uses fusedLocationClient to continually grab user location and store on firebase
    fun startShareLocation() {
        locationManager.startLocationUpdates()
        showSnackbar(R.string.sharing_loc)
    }

    //turns off fusedLocationClient
    fun stopShareLocation() {
        locationManager.stopLocationUpdates()
        repo.clearLocation()
        showSnackbar(R.string.stop_sharing_loc)
    }

    //check if permission is granted
    fun permissionsGranted(_permissions : Array<String>) : Boolean {

        if(android.os.Build.VERSION.SDK_INT >= 26) {
//            Log.d("map", "API version >= 26")

            return _permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PERMISSION_GRANTED
            }
        } else {
//            Log.d("map", "API version <= 26")
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //if user has not granted background location permission, stops sharing location onPause
    override fun onPause() {

        if((locationManager.updatingLocation.value == true) &&
            (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED))) {
                Log.d("map", "on pause, stopping background update")
                stopShareLocation()
                switchShare.isChecked = false
            }
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.btnSignOut) {
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    //turns off auth state listener , signs out, and redirects to sign-in page
    fun signOut() {

        //reset status - remove listeners, turn off switch and location sharing
        auth.removeAuthStateListener(authStateListener)
        switchShare.isChecked = false

        //signs out and  returns to Sign in page
        auth.signOut()
        Intent(this, SignInActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also{ startActivity(it) }

        finish()
    }

    //show notification snack bar, used when start/stop sharing location
    fun showSnackbar(stringId : Int) {

        val containerView = findViewById<View>(R.id.home_view)
        Snackbar.make(containerView, stringId, Snackbar.LENGTH_LONG)
            .setAnchorView(switch_container_view)
            .show()
    }

    //save state of switch for orientation change or when another activity is started
    override fun onDestroy() {

        val isChecked = switchShare.isChecked

        val prefs = getSharedPreferences("shareStatus", 0)
        prefs.edit().putBoolean("isSharing", isChecked).apply()

        super.onDestroy()
    }




}