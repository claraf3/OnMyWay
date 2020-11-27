package com.clara.onmyway.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.clara.onmyway.*
import com.clara.onmyway.adapter.FriendRecyclerViewAdapter
import com.clara.onmyway.data.Friend
import com.clara.onmyway.callbacks.AddFriendListener
import com.clara.onmyway.callbacks.FriendListListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_friend_list.*

class FriendListActivity :
    AppCompatActivity(),
    AddFriendListener,
    FriendListListener
{
    private lateinit var user : FirebaseUser
    private var auth = FirebaseAuth.getInstance()

    private val repo = FirebaseRepoImpl()

    private var isSharingLoc = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        //set toolbar
        setSupportActionBar(toolbar_frdList)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //check if user was previously sharing location before orientation change
        if(intent != null && intent.hasExtra("isSharingLoc")) {
            isSharingLoc = intent.getBooleanExtra("isSharingLoc", false)
        }

        //check firebase authentication
        val currentUser = auth.currentUser
        if(currentUser == null) {
            Log.d("auth", "user not logged in yet")

            Intent(this, SignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also{ startActivity(it) }
            finish()
        } else {
            user = currentUser
            Log.d("auth", "user logged in, $currentUser")
        }

        //retrieve and display friend list
        populateFriends()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        toolbar_frdList.setNavigationIcon(R.drawable.back_icon)
        toolbar_frdList.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }
        menuInflater.inflate(R.menu.friend_list_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.btnAddFriend) {
            startAddFriendFragment()
        }
        if(item.itemId == R.id.btnFrdRequests) {
            val intent = Intent(this, RequestsListActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    //queries firebase for a list of friends
    fun populateFriends() {

        Log.d("friend", "entered populateFriends()")

        repo.getFriendList(this)

    }

    //callback if successfully retrieve list of friends stored on database
    override fun onGetFriendsSuccess(friends: MutableList<Friend>) {
        displayFriends(friends)
    }

    //callback if user has no friends
    override fun onNoFriends() {
        friendRecyclerView.visibility = View.GONE
        noFriendsView.visibility = View.VISIBLE
    }

    //fragment allows user to input the username to add as friend
    private fun startAddFriendFragment() {

        val currentFragment  = supportFragmentManager.findFragmentByTag("FRIEND_FRAG")
        if(currentFragment == null) {
            AddFriendFragment()
                .show(supportFragmentManager, "FRIEND_FRAG")
        }

    }

    //callback when a friend request has been sent
    override fun onRequestFriendSuccess(username : String) {
        Toast.makeText(this, resources.getString(R.string.add_friend_feedback) + " $username", Toast.LENGTH_LONG).show()
    }

    //callback for request to start observing another user's location
    override fun requestObserveLocation(username: String) {
        repo.requestObserve(username, this)
    }

    //callback when request to observe to successful
    override fun onAllowObserve(username: String) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    //callback when request to observe to denied
    override fun onDenyObserve() {
        showSnackbar(R.string.not_online)
    }

    //called when user changes friend's "allow" status
    override fun changeAllowObserveStatus(username: String, allow: Boolean, switch: SwitchCompat) {

        //if user turns off allow observe on friend but is currently sharing location, request user
        //to first turn off share before turning off allow
        if(!allow && isSharingLoc) {

            switch.isChecked = true
            showSnackbar(R.string.allow_error)

        } else {
            repo.updateAllowStatus(username, allow)
        }
    }

    fun showSnackbar(stringId : Int) {

        val view = findViewById<View>(R.id.friendListView)
        Snackbar.make(view, stringId, Snackbar.LENGTH_LONG)
            .setAction(R.string.confirm) {
                it.visibility = View.GONE
            }
            .show()
    }

    //set recycler view adapter to display list of friends
    fun displayFriends(friends : MutableList<Friend>) {

        friendRecyclerView.visibility = View.VISIBLE
        noFriendsView.visibility = View.GONE

        val adapter =
            FriendRecyclerViewAdapter(
                friends,
                this@FriendListActivity
            )
        friendRecyclerView.adapter = adapter
    }

    //callback for recycler holder 'chat' button
    override fun startChat(username: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

}