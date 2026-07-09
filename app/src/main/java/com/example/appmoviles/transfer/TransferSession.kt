package com.example.appmoviles.transfer

data class TransferSession(

    val videoId: String,

    var bytesTransferred: Long = 0,

    var finished: Boolean = false
)