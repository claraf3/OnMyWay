package com.clara.onmyway.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R
import com.clara.onmyway.data.Friend
import com.clara.onmyway.callbacks.FriendListListener

class FriendRecyclerViewAdapter(private val friends : MutableList<Friend>, private val listener : FriendListListener) : RecyclerView.Adapter<FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_row_layout, parent, false)

        return FriendViewHolder(view)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {

        val user = friends[position]

        holder.usernameView.text = user.username
        holder.firstNameView.text = user.fName
        holder.lastNameView.text = user.lName

        val allow : Boolean = user.allow

        when(allow) {
            true -> holder.allowSwitch.isChecked = true
            false -> holder.allowSwitch.isChecked = false
        }

        holder.setClickListeners(listener, user.username)

    }

}