package com.clara.onmyway.callbacks

interface ObserverListListener {

    fun onObserverUpdated(observers : MutableList<String>)

    fun onNoObservers()
}