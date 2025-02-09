package com.garlicbread.foodbridge.dto

data class User(
    val user_id: UserData
)

data class UserData(
    val id: String,
    val name: String,
    val address: String,
    val contact: String,
    val email: String,
    val token_count: Int
)