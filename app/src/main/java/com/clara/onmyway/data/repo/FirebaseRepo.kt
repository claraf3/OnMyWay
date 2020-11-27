package com.clara.onmyway.data.repo

import com.clara.onmyway.data.Location
import com.clara.onmyway.callbacks.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

interface FirebaseRepo {

    //sign up functions
    fun userSignUp(listener: SignUpListener, email : String, password : String, username : String, firstName : String, lastName : String)

    fun storeUserInfo(username : String, firstName : String, lastName : String, listener : SignUpListener)

    fun checkIfUsernameExists(email : String, password : String, username : String, firstName : String, lastName : String, listener : SignUpListener)

    //sign in functions
    fun signIn(email : String, password : String, listener : SignInListener)

    //friend functions
    fun validateFriendRequest(username: String, frag : AddFriendListener, activity : AddFriendListener)

    fun addFriendRequest(username : String, activity :AddFriendListener)

    fun getFriendRequests(listener : RequestListListener)

    fun addFriend(username : String, listener : RequestListListener)

    fun denyFriendRequest(username : String, listener: RequestListListener)

    fun getFriendList(listener : FriendListListener)

    fun updateAllowStatus(username: String, allow : Boolean)

    fun requestObserve(username : String, listener: FriendListListener)

    fun storeChatMessage(cmsg: ChatMessage, query : DatabaseReference, chatId : String)

    //location functions
    fun updateLocation(location : Location)

    fun clearLocation()

    fun registerObserver(username : String)

    fun removeObserver(username : String)

    fun getObservers(listener : ObserverListListener, fbRef : DatabaseReference) : ValueEventListener

    fun getLocation(listener : MapsActivityListener, fbRef : DatabaseReference) : ValueEventListener


}