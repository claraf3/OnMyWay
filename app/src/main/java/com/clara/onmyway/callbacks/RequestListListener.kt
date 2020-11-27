package com.clara.onmyway.callbacks

interface RequestListListener {

    fun onGetRequestComplete(requests : MutableList<String>)

    fun onNoRequests()

    fun onAddFriendSuccess(username : String)

    fun onAddFriendError(errorMsg : String?)

    fun onDenyRequest(username : String)

}