package com.clara.onmyway.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R

class ChatHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

    val msgView = itemView.findViewById<TextView>(R.id.msgView)
}