package com.clara.onmyway.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R

class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val usernameView = itemView.findViewById<TextView>(R.id.requestUsernameView)
    val btnAccept = itemView.findViewById<Button>(R.id.btnAccept)
    val btnDeny = itemView.findViewById<Button>(R.id.btnDeny)

    fun setListeners(listener : RequestRecyclerViewAdapter.RequestViewListener, username : String) {

        //adds user to friend list and removes request
        btnAccept.setOnClickListener {
            listener.acceptRequest(username)
        }

        //removes request
        btnDeny.setOnClickListener {
            listener.denyRequest(username)
        }
    }
}