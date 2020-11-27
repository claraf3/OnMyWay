package com.clara.onmyway.callbacks

interface SignUpListener {

    fun onCreateUserError(errorMsg : String)

    fun showUserNameTakenError()

    fun onCreateUserSuccess()

}