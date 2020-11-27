package com.clara.onmyway.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.clara.onmyway.R
import com.clara.onmyway.callbacks.AddFriendListener
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.google.firebase.auth.FirebaseAuth


class AddFriendFragment : DialogFragment(), AddFriendListener {

    private val repo = FirebaseRepoImpl()

    private lateinit var listener : AddFriendListener
    private lateinit var usernameInput : EditText

    override fun onAttach(context: Context) {
        if(context is AddFriendListener) {
            listener = context
        }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usernameInput = view.findViewById<EditText>(R.id.frdUsernameInput)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        btnConfirm.setOnClickListener {

            closeKeyboard()

            //
            val username = usernameInput.text.toString().trim()
            if(!username.isEmpty() && !username.equals(FirebaseAuth.getInstance().currentUser!!.displayName) && !username.contains(".")) {
                //checks if username exists
                repo.validateFriendRequest(username, this, listener)
            } else {
                Toast.makeText(context,
                    R.string.username_invalid, Toast.LENGTH_LONG).show()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    //callback if friend has already been addeed
    override fun friendAlreadyAdded() {
        Toast.makeText(context, resources.getString(R.string.friend_exists), Toast.LENGTH_SHORT).show()
    }

    //callback if username is not found
    override fun usernameNotFound() {
        Toast.makeText(context,
            R.string.username_not_found, Toast.LENGTH_SHORT).show()
    }

    override fun clearInput() {
        usernameInput.text.clear()

    }

    //closes soft input keyboard
    fun closeKeyboard() {

        val view = usernameInput

        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


    }
}