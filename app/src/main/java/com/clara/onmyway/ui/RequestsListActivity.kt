package com.clara.onmyway.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.clara.onmyway.R
import com.clara.onmyway.adapter.RequestRecyclerViewAdapter
import com.clara.onmyway.callbacks.RequestListListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_request_list.*

class RequestsListActivity : AppCompatActivity(), RequestRecyclerViewAdapter.RequestViewListener, RequestListListener{

    private var requests = mutableListOf<String>()

    private val auth = FirebaseAuth.getInstance()
    private lateinit var user : FirebaseUser

    private var repo = FirebaseRepoImpl()

    private lateinit var adapter : RequestRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_list)

        //set toolbar
        setSupportActionBar(request_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //check firebase user authentication
        val currentUser = auth.currentUser
        if(currentUser == null) {
            Log.d("auth", "user not logged in yet")
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            user = currentUser
            Log.d("auth", "user logged in, $currentUser")
        }

        //set adapter
        adapter =
            RequestRecyclerViewAdapter(
                requests,
                this@RequestsListActivity
            )
        requestsRecyclerView.adapter = adapter

        populateRequests()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        request_toolbar.setNavigationIcon(R.drawable.back_icon)
        request_toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }
        menuInflater.inflate(R.menu.empty_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //populates list of requests
    fun populateRequests() {

        requestProgressBar.visibility = View.VISIBLE
        repo.getFriendRequests(this)

    }

    //populate recycler view with friend requests
    override fun onGetRequestComplete(_requests : MutableList<String>) {

        requests = _requests

        updateView()
        requestProgressBar.visibility = View.INVISIBLE
    }

    //make recycler view invisible, show message that there are no friend requests
    override fun onNoRequests() {

        updateView()
        requestProgressBar.visibility = View.INVISIBLE

    }

    //callback if user clicks 'accept' button
    override fun acceptRequest(username: String) {

        Log.d("friend", "accept request from $username")
        repo.addFriend(username, this)

    }

    //callback when friend is successfully added on firebase
    override fun onAddFriendSuccess(username : String) {

        Toast.makeText(this,
            R.string.add_success, Toast.LENGTH_SHORT).show()

        //remove request from list and update view
        requests.remove(username)
        updateView()
    }

    //callback when failed to add friend on firebase
    override fun onAddFriendError(errorMsg: String?) {

        when(errorMsg) {
            null ->  Toast.makeText(this,
                R.string.add_fail, Toast.LENGTH_SHORT).show()
            else -> Log.d("error", "failed to get user info $errorMsg")
        }

    }

    //callback if user clicks 'deny' button
    override fun denyRequest(username: String) {
        Log.d("friend", "deny request from $username")
        repo.denyFriendRequest(username, this)
    }

    //callback when friend request has been removed from firebase
    override fun onDenyRequest(username: String) {

        Toast.makeText(this,
            R.string.deny_success, Toast.LENGTH_SHORT).show()

        //remove request from list and update view
        requests.remove(username)
        updateView()
    }


    //to update list view
    fun updateView() {

        Log.d("log", "request adapter view updated")
        if(requests.isEmpty()) {
            requestsRecyclerView.visibility = View.GONE
            noRequestsView.visibility = View.VISIBLE
        } else {
            requestsRecyclerView.visibility =  View.VISIBLE
            noRequestsView.visibility = View.GONE

            adapter.setItems(requests)
            adapter.notifyDataSetChanged()
        }
    }

}

