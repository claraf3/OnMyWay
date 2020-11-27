package com.clara.onmyway.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.clara.onmyway.R
import com.clara.onmyway.adapter.ObserverRecyclerViewAdapter
import com.clara.onmyway.callbacks.ObserverListListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_observer_list.*

class ObserverListActivity : AppCompatActivity(), ObserverListListener {

    private val auth = FirebaseAuth.getInstance()
    private val user : FirebaseUser? = auth.currentUser

    private val repo = FirebaseRepoImpl()
    private val firebaseRef = Firebase.database.reference.child("Users/${user?.displayName}/observers")
    private val observers = mutableListOf<String>()
    private lateinit var adapter : ObserverRecyclerViewAdapter

    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observer_list)

        //set action bar
        setSupportActionBar(observer_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //store firebase event listener for destruction later on
        valueEventListener = getListOfOBservers()

        //set adatper for recycler view
        adapter = ObserverRecyclerViewAdapter(observers)
        observerRecyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        observer_toolbar.setNavigationIcon(R.drawable.back_icon)
        observer_toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        menuInflater.inflate(R.menu.empty_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //call repo to start listening on who is observering this user's location
    fun getListOfOBservers(): ValueEventListener {

        return repo.getObservers(this, firebaseRef)
    }

    //call back when the the list of observers has changed
    override fun onObserverUpdated(observers: MutableList<String>) {
        populateObservers(observers)
    }

    //callback if no one is listening on user's location
    override fun onNoObservers() {
//        Log.d("obs", "no observers")
        noObserversView.visibility = View.VISIBLE
        observerRecyclerView.visibility = View.GONE
    }

    //load updated list of observers onto recycler view
    fun populateObservers(observers : MutableList<String>) {

        noObserversView.visibility = View.GONE
        observerRecyclerView.visibility = View.VISIBLE

        adapter.setItems(observers)
        adapter.notifyDataSetChanged()
    }

    //removes firebase listener
    override fun onDestroy() {
        firebaseRef.removeEventListener(valueEventListener)
        super.onDestroy()
    }


}