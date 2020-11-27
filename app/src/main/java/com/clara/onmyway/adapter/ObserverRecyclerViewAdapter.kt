package com.clara.onmyway.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clara.onmyway.R

class ObserverRecyclerViewAdapter(private var observers : MutableList<String>) : RecyclerView.Adapter<ObserverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObserverViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.observer_row_layout, parent, false)

        return ObserverViewHolder(view)
    }

    override fun getItemCount(): Int {
        return observers.count()
    }

    override fun onBindViewHolder(holder: ObserverViewHolder, position: Int) {

        holder.usernameView.text = observers[position]
    }

    fun setItems(_observers : MutableList<String>) {
        observers = _observers
    }

}