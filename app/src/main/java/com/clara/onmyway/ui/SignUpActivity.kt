package com.clara.onmyway.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.clara.onmyway.R
import com.clara.onmyway.callbacks.SignUpListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.email_input
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(),
    SignUpListener {

    private lateinit var auth : FirebaseAuth

    private var repo : FirebaseRepoImpl = FirebaseRepoImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSignUp.setOnClickListener {
            validateInputs()
        }

        auth = FirebaseAuth.getInstance()
    }

    //checks user input and calls signUp() if all input is validated
    fun validateInputs() {

        //validate email entry
        val email = email_input.text.toString().trim()
        val validPattern : Boolean = Patterns.EMAIL_ADDRESS.matcher((email)).matches()

        if(email.isEmpty() || !validPattern) {
            Toast.makeText(this, resources.getString(R.string.email_invalid), Toast.LENGTH_SHORT).show()
            return
        }

        //validate password entry
        val passwordOne = password_input_1.text.toString().trim()
        val passwordTwo = password_input_2.text.toString().trim()

        if(passwordOne.isEmpty() || passwordTwo.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.password_invalid_signup), Toast.LENGTH_SHORT).show()
            return
        } else if(!passwordOne.equals(passwordTwo)) {
            Toast.makeText(this, resources.getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        //validate username
        val username = username_input.text.toString().trim()
        if(username.isEmpty() || username.contains(".")) {
            Toast.makeText(this, resources.getString(R.string.username_invalid), Toast.LENGTH_SHORT)
                .show()
            return
        }

        //validate first name and last name
        val firstName = firstName_input.text.toString().trim()
        val lastName = lastName_input.text.toString().trim()
        if(firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.name_invalid), Toast.LENGTH_SHORT)
                .show()
            return
        }

        signUpProgressBar.visibility = View.VISIBLE
        //check if username has already been taken, if not, create the account.
        repo.checkIfUsernameExists(email, passwordOne, username, firstName, lastName, this)
    }

    //callback if username has already been taken
    override fun showUserNameTakenError() {
        signUpProgressBar.visibility = View.INVISIBLE
        Toast.makeText(baseContext, resources.getString(R.string.username_taken), Toast.LENGTH_LONG).show()
    }

    //handles successful user storage into database
    override fun onCreateUserSuccess() {

        signUpProgressBar.visibility = View.INVISIBLE
        Log.d("auth", "store user success")
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    //handles failed user creation, creates toast message notifying the cause of error
    override fun onCreateUserError(errorMsg : String) {

        signUpProgressBar.visibility = View.INVISIBLE
        Log.d("auth", "sign up failed")
        Log.d("auth", "$errorMsg")

        when(errorMsg) {
            "com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]"
            -> Toast.makeText(this, resources.getString(R.string.password_too_short), Toast.LENGTH_LONG).show()
            "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account."
            -> Toast.makeText(this, resources.getString(R.string.email_taken), Toast.LENGTH_LONG).show()
        }
    }

}