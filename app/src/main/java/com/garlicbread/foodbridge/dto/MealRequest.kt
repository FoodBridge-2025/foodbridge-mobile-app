package com.garlicbread.foodbridge.dto

data class MealRequest(
    val id: String,
    val servings: Int,
    val date: String,
    val meal_type: String,
    val status: String,
    val community_centre: CommunityCentre
)