package com.clara.onmyway.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R
import com.clara.onmyway.adapter.ChatAdapter
import com.clara.onmyway.data.repo.ChatMessage
import com.clara.onmyway.data.repo.FirebaseRepoImpl
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
import java.time.Instant
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    private val user = FirebaseAuth.getInstance().currentUser

    private var chatId : String? = null
    private lateinit var username : String
    private val repo = FirebaseRepoImpl()
    private lateinit var query : DatabaseReference

    private lateinit var adapter: ChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(chat_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if(intent != null && intent.hasExtra("username")) {
            username = intent.getStringExtra("username").toString()
            titleView.text = username
        }

        chatId = getChatId()

        if(chatId != null) {

            //set database reference for chat messages
            query = Firebase.database.reference.child("Chat/$chatId")

            //set chat adapter
            val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query.limitToLast(50), ChatMessage::class.java)
                .build()

            adapter = ChatAdapter(options)

            chatRecyclerView.adapter = adapter

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {

                    chatRecyclerView.scrollToPosition(adapter.itemCount.minus(1))
                }
            })

            //set click listener to send message
            btnSend.setOnClickListener {
                storeToFb()
            }
        } else {
            Log.d("chat", "user not found , cannot start chat")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        chat_toolbar.setNavigationIcon(R.drawable.back_icon)
        chat_toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        menuInflater.inflate(R.menu.empty_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //validates message and store on database
    fun storeToFb() {

        val msg = msgInput.text.trim().toString()

        if(!msg.isEmpty() && !chatId.isNullOrEmpty()) {

            val timeStamp : String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            val cm = ChatMessage(msg, user?.displayName.toString(), timeStamp)

            repo.storeChatMessage(cm, query, chatId!!)

            msgInput.text.clear()
        }
    }

    //get chat id between the two users by combining their usernames
    fun getChatId() : String? {

        val user = FirebaseAuth.getInstance().currentUser?.displayName

        if(user != null) {

            if (user.compareTo(username) > 0) {
                return "$user$username"
            } else {
                return "$username$user"
            }

        } else return null
    }

    //starts FirebaseRecyclerAdapter to listen on chat database
    override fun onStart() {

        super.onStart()
        adapter.startListening()

    }

    //stops FirebaseRecyclerAdapter to listen on chat database
    override fun onStop() {

        super.onStop()
        adapter.stopListening()
    }

    //closes soft keyboard when user clicks outside EditText for message input
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if(ev?.action == MotionEvent.ACTION_DOWN) {

            val view = currentFocus
            if(view is EditText) {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(ev)
    }


}