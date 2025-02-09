package com.garlicbread.foodbridge.dto

data class DonationItem(
    val title: String,
    val description: String,
    val image: String,
    val servings: Int,
    val user_id: String,
    val request_id: String
)
