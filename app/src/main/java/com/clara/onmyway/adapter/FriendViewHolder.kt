package com.clara.onmyway.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R
import com.clara.onmyway.callbacks.FriendListListener

class FriendViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

    val usernameView = itemView.findViewById<TextView>(R.id.usernameView)
    val firstNameView = itemView.findViewById<TextView>(R.id.firstNameView)
    val lastNameView = itemView.findViewById<TextView>(R.id.lastNameView)
    val allowSwitch = itemView.findViewById<SwitchCompat>(R.id.switchAllow)

    val btnChat = itemView.findViewById<Button>(R.id.btnChat)

    fun setClickListeners(listener : FriendListListener, username : String) {
        itemView.setOnClickListener {
            listener.requestObserveLocation(username)
        }

        allowSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                listener.changeAllowObserveStatus(username, true, allowSwitch)
            } else {
                listener.changeAllowObserveStatus(username, false, allowSwitch)
            }
        }

        btnChat.setOnClickListener {
            listener.startChat(username)
        }

    }
}