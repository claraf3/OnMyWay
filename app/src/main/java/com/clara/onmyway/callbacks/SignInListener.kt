package com.clara.onmyway.callbacks

interface SignInListener {

    fun onSignInSuccess()

    fun onSignInError(errorMsg : String)

}