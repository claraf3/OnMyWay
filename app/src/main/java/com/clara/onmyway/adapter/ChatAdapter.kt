package com.clara.onmyway.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.clara.onmyway.R
import com.clara.onmyway.data.repo.ChatMessage
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(options : FirebaseRecyclerOptions<ChatMessage>) : FirebaseRecyclerAdapter<ChatMessage, ChatHolder>(options) {

    override fun getItemViewType(position: Int): Int {

        val msg = getItem(position)

        val user = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        if(msg.sender.equals(user)) {
            return R.layout.chat_sender_layout
        } else {
            return R.layout.chat_receiver_layout
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {

        Log.d("chat", "viewType is $viewType")
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int, model: ChatMessage) {

        Log.d("chat", "entered onBindViewHolder")
        val chatMsg = model.message

        holder.msgView.text = chatMsg

    }

}