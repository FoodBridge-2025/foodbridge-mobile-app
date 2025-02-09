package com.garlicbread.foodbridge.dto

data class CommunityCentreRequests(
    val id: String,
    val title: String,
    val quantity: Int,
    val latitude: Long,
    val longitude: Long,
)
