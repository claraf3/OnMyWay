package com.clara.onmyway.callbacks

import androidx.appcompat.widget.SwitchCompat
import com.clara.onmyway.data.Friend

interface FriendListListener {

    fun onGetFriendsSuccess(friends : MutableList<Friend>)

    fun onNoFriends()

    fun requestObserveLocation(username : String)

    fun changeAllowObserveStatus(username: String, allow : Boolean, switch: SwitchCompat)

    fun onAllowObserve(username : String)

    fun onDenyObserve()

    fun startChat(username: String)
}