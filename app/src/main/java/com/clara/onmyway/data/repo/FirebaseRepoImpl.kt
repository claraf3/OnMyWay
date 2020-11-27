package com.clara.onmyway.data.repo

import android.util.Log
import com.clara.onmyway.data.Location
import com.clara.onmyway.callbacks.*
import com.clara.onmyway.data.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRepoImpl : FirebaseRepo {

    private var firebaseRef = Firebase.database.reference
    private var auth = FirebaseAuth.getInstance()
    private var user = auth.currentUser
    private var userRef = firebaseRef.child("Users")

    //checks if username has already been used for account creation
    override fun checkIfUsernameExists(email : String, password : String, username : String, firstName : String, lastName : String, listener : SignUpListener) {

        userRef.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                //do nothing
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(username)) {
                    listener.showUserNameTakenError()
                } else {
//                    listener.signUp(email, password, username, firstName, lastName)
                    userSignUp(listener, email, password, username, firstName, lastName)
                }
            }
        })
    }

    //adds user account onto firebase
    override fun userSignUp(listener: SignUpListener, email: String, password: String, username: String, firstName: String, lastName: String
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if(it.isSuccessful) {
                Log.d("auth", "account created was successful")

                storeUserInfo(username, firstName, lastName, listener)
            } else {

                listener.onCreateUserError(it.exception.toString())

            }
        }
    }

    //stores/updates username, first name, and lastname into firebase (/Users/${user.displayName}/)
    override fun storeUserInfo(username: String, firstName: String, lastName: String, listener : SignUpListener) {

        Log.d("auth", "store username entered")

        if(auth.currentUser != null) {

            val user = auth.currentUser

            //store username into firebase authentication user object
            val updater = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user!!.updateProfile(updater)

            //store other user info into firebase realtime database
            val userInfo = mapOf<String, String>(
                "email" to user.email.toString(),
                "id" to user.uid,
                "firstname" to firstName,
                "lastname" to lastName
            )

            //add to firebase
            val userAccount = mapOf<String,Map<String, String>>(
                "/Users/$username" to userInfo
            )
            val theUsername = mapOf<String,String>(
                "/Usernames/${user.uid}" to username
            )

            firebaseRef.updateChildren(theUsername)
            firebaseRef.updateChildren(userAccount).addOnCompleteListener {
                Log.d("auth", "update info successful")
                listener.onCreateUserSuccess()
            }

        } else {
            Log.d("auth","failed to store user info -- user not found")
        }


    }

    //sign in with firebase authentication user email/password
    override fun signIn(email : String, password : String, listener : SignInListener) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    listener.onSignInSuccess()
                } else {
                    listener.onSignInError(it.exception.toString())

                }
            }
    }

    //checks if username to add as friend is valid (if exists and not already added in friend's list)
    override fun validateFriendRequest(username: String, frag : AddFriendListener, activity : AddFriendListener) {

        if(user != null) {

            firebaseRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(error: DatabaseError) {
                    Log.d("log", "failed to validate friend request")
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    //check if friend already added to list
                    val friendExists = snapshot.child("${user!!.displayName}/friends").hasChild(username)

                    when {
                        friendExists -> frag.friendAlreadyAdded() //notify user that friend is already added
                        snapshot.hasChild(username) -> addFriendRequest(username, activity).also { frag.clearInput() } //add friend
                        else -> frag.usernameNotFound() //notify user the username to add does not exist
                    }
                }
            })
        }
    }

    //adds the friend request to database
    override fun addFriendRequest(username: String, activity :AddFriendListener) {

        val ref = firebaseRef.child("Users/$username/requests")

        val user = auth.currentUser
        if(user != null) {

            val request = mapOf<String, String>(
                "${user.displayName}" to "${user.displayName}"
            )

            ref.updateChildren(request)

            activity.onRequestFriendSuccess(username)
        }

    }

    //retrieve the friend requests received by user
    override fun getFriendRequests(listener : RequestListListener) {

        if(user != null) {

            val requests = mutableListOf<String>()
            firebaseRef.child("Users/${user!!.displayName.toString()}/requests").addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {

                        for(child in snapshot.children) {
                            requests.add(child.value.toString())
                        }
                        listener.onGetRequestComplete(requests)
                    }
                    else {
                        listener.onNoRequests()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("error", "failed to retrieve list of requests")
                    listener.onNoRequests()
                }
            })
        }
    }

    //adds the requester and receiver's usernames under each other's friends list
    override fun addFriend(username: String, listener: RequestListListener) {

        if (user != null) {

            firebaseRef.child("Users/${user!!.displayName}/requests/$username").removeValue()

            //retrieve request user info and add to user's friend list
            firebaseRef.child("Users/$username").addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val fname = snapshot.child("firstname").value.toString()
                    val lname = snapshot.child("lastname").value.toString()

                    val info = mapOf(
                        "firstname" to fname,
                        "lastname" to lname,
                        "allow" to true
                    )

                    firebaseRef.child("Users/${user!!.displayName}/friends/$username")
                        .updateChildren(info)
                        .addOnCompleteListener {
                            listener.onAddFriendSuccess(username)
                        }
                        .addOnCanceledListener {
                            listener.onAddFriendError(null)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onAddFriendError(error.message)
                }
            })

            //retrieve receiver user info and add to requester's friend list
            firebaseRef.child("Users/${user!!.displayName}").addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val fname = snapshot.child("firstname").value.toString()
                    val lname = snapshot.child("lastname").value.toString()

                    val info = mapOf(
                        "firstname" to fname,
                        "lastname" to lname,
                        "allow" to true
                    )

                    firebaseRef.child("Users/$username/friends/${user!!.displayName}").updateChildren(info)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("error", "failed to get user info ${error.message}")
                }
            })
        }
    }

    //deny friend request, removes request from database
    override fun denyFriendRequest(username : String, listener: RequestListListener) {

        if(user != null) {

            firebaseRef.child("Users/${user!!.displayName}/requests/$username")
                .removeValue()
                .addOnCompleteListener {
                    listener.onDenyRequest(username)
                }
        }
    }

    //updates whether the user allows a friend to listen in on his location when he is sharing loc
    override fun updateAllowStatus(username: String, allow: Boolean) {
        if(user != null) {

            val allowStatus = mapOf (
                "allow" to allow
            )

            firebaseRef.child("Users/${user!!.displayName}/friends/$username").updateChildren(allowStatus)
        }
    }

    //sends request to listen on friend's location -- checks if this user is allowed to listen in and if friend is currently sharing location
    override fun requestObserve(username: String, listener : FriendListListener) {
        if(user != null) {

//            firebaseRef.child("Users/$username/friends/${user!!.displayName}/allow").addListenerForSingleValueEvent(object : ValueEventListener {
            firebaseRef.child("Users/$username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    listener.onDenyObserve()
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    val isOnline = snapshot.child("location/online").value.toString().toBoolean()
                    val isAllow = snapshot.child("friends/${user!!.displayName}/allow").value.toString().toBoolean()

                    if(isOnline && isAllow) {
                        listener.onAllowObserve(username)
                    } else {
                        listener.onDenyObserve()
                    }
                }
            })

        }
    }

    //when user is listening on friend's location, will register user as the friend's observer
    override fun registerObserver(username : String) {

        if(user != null) {

            val observer = mapOf<String, String>(
                "${user!!.displayName}" to "${user!!.displayName}"
            )

            firebaseRef.child("Users/$username/observers").updateChildren(observer)
        }
    }

    //when user stop listening on friend's location, will remove user as the friend's observer
    override fun removeObserver(username : String) {

        if(user != null) {
            firebaseRef.child("Users/$username/observers/${user!!.displayName}").removeValue()
        }
    }

    //for users to retrieve list of friends who are currently observing his location
    override fun getObservers(listener: ObserverListListener, fbRef : DatabaseReference) : ValueEventListener {

        val valueEventListener = object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Log.d("error", "failed to get observers")
                listener.onNoObservers()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("obs", "got snapshot")

                val observers = mutableListOf<String>()

                for(child in snapshot.children) {
                    observers.add(child.value.toString())
                }

                if(observers.size == 0) {
                    Log.d("obs", "no observers")
                    listener.onNoObservers()

                } else {
                    listener.onObserverUpdated(observers)
                }
            }
        }

        if(user != null) {
            fbRef.addValueEventListener(valueEventListener)
        }

        return valueEventListener
    }

    //stores updated location to firebase when user is sharing location
    override fun updateLocation(location: Location) {

        if(user != null) {
            firebaseRef.child("Users/${user!!.displayName}/location").setValue(location)
                .addOnCompleteListener {
                    Log.d("map", "Location logged on fb with ${location.toString()}")
                }
                .addOnCanceledListener {
                    Log.d("map", "failed to update location")
            }
        }
    }

    //clears user's location data from firebase when user stops sharing location
    override fun clearLocation() {

        if(user != null) {
            val location =
                Location(0.0, 0.0, false)
            firebaseRef.child("Users/${user!!.displayName}/location").setValue(location)
        }
    }

    //for user to retrieve list of friends
    override fun getFriendList(listener : FriendListListener) {

        if(user != null) {

            userRef.child("${user!!.displayName}/friends").addListenerForSingleValueEvent(object :
                ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val friends = mutableListOf<Friend>()
                    Log.d("friend", "has friends : ${snapshot.hasChildren()}")

                    for(child in snapshot.children) {

                        val username = child.key.toString()
                        val firstName = child.child("firstname").value.toString()
                        val lastName = child.child("lastname").value.toString()
                        val allow = child.child("allow").value.toString().toBoolean()

                        friends.add(
                            Friend(username, firstName, lastName, allow)
                        )
                    }

                    if(friends.size == 0) {
                        listener.onNoFriends()
                    } else {
                        //set adapter and UI view
                        listener.onGetFriendsSuccess(friends)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onNoFriends()
                    Log.d("log", "error while reading firebase database from FriendListActivity")
                }
            })
        }
    }

    //for user to retrieve and start listening to friend's location
    override fun getLocation(listener: MapsActivityListener, fbRef: DatabaseReference): ValueEventListener {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                listener.onUserOffline()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.child("online").value == true) {
                    Log.d("map", "Location change detected")

                    listener.onLocationUpdated(Location(snapshot.child("lat").value.toString().toDouble(), snapshot.child("lng").value.toString().toDouble(), true))

                } else {
                    listener.onUserOffline()

                }
            }
        }

        fbRef.addValueEventListener(valueEventListener)
        return valueEventListener
    }


    //stores chat message under chat id using auto key generation with push()
    override fun storeChatMessage(cmsg: ChatMessage, query : DatabaseReference, chatId : String) {

        query.push().setValue(cmsg)
    }


}