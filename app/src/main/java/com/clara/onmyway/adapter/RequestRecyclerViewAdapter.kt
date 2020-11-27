package com.clara.onmyway.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R

class RequestRecyclerViewAdapter(private var requests : MutableList<String>, private val listener : RequestViewListener) : RecyclerView.Adapter<RequestViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_row_layout, parent, false)

        return RequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {

        val username = requests[position]
        holder.usernameView.text = username

        holder.setListeners(listener, username)
    }

    //to set updated data
    fun setItems(_requests : MutableList<String>) {
        requests = _requests
    }

    interface RequestViewListener {
        fun acceptRequest(username: String)
        fun denyRequest(username: String)
    }


}