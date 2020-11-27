package com.clara.onmyway.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.clara.onmyway.R
import com.clara.onmyway.callbacks.SignInListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

//const val RC_SIGN_IN = 300
class SignInActivity : AppCompatActivity(), SignInListener{

    private lateinit var auth : FirebaseAuth
    private var repo = FirebaseRepoImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //button listeners
        btnSignIn.setOnClickListener {
            validateCredentialInput()
        }

        //starts sign up activity
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        //check firebase user authentication
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if(currentUser != null) {
            Log.d("auth", "user logged in, $currentUser")
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("auth", "user not logged in yet")
        }

    }

    //validate email/password input
    fun validateCredentialInput() {

        //validate email input
        val email = email_input.text.toString().trim()
        val validPattern : Boolean = Patterns.EMAIL_ADDRESS.matcher((email)).matches()

        if(email.isEmpty() || !validPattern) {
            Toast.makeText(this, resources.getString(R.string.email_invalid), Toast.LENGTH_LONG).show()
            return
        }

        //validate password input
        val passwordOne = password_input.text.toString().trim()
        if(passwordOne.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.password_invalid_signin), Toast.LENGTH_LONG).show()
            return
        }

        signIn(email, passwordOne)

    }

    //calls repo to proccess firebase sign in authentication
    fun signIn(email : String, password : String) {

        progressBar.visibility = View.VISIBLE

        repo.signIn(email, password, this)

    }

    //callback if sign in success, redirects to home activity
    override fun onSignInSuccess() {

        progressBar.visibility = View.INVISIBLE
        Log.d("auth", "sign in success, user is ${auth.currentUser}")
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()

    }

    //callback if sign in error
    override fun onSignInError(errorMsg: String) {

        progressBar.visibility = View.INVISIBLE
        when(errorMsg) {
            "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted."
            -> Toast.makeText(this, resources.getString(R.string.user_not_found), Toast.LENGTH_LONG).show()
            "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password."
            -> Toast.makeText(this, resources.getString(R.string.password_incorrect), Toast.LENGTH_LONG).show()
        }
        Log.d("auth", "sign in failed")
        Log.d("auth", "$errorMsg")

    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(requestCode == RC_SIGN_IN) {
//            if(resultCode == Activity.RESULT_OK) {
//
//            } else {
//                Log.d("auth", "sign in failed")
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
}