package com.clara.onmyway.data.repo

data class ChatMessage (val message : String, val sender : String, val timestamp : String){

    //default constructor for FirebaseRecyclerAdapter
    constructor() : this("","","")

}