package com.clara.onmyway.callbacks

interface AddFriendListener {

    fun friendAlreadyAdded() {}

    fun usernameNotFound() {}

    fun onRequestFriendSuccess(username : String) {}

    fun clearInput() {}

}