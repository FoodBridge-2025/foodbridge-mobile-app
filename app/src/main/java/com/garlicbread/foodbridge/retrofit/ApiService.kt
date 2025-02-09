package com.garlicbread.foodbridge.retrofit

import com.garlicbread.foodbridge.dto.CommunityCentre
import com.garlicbread.foodbridge.dto.DonationItem
import com.garlicbread.foodbridge.dto.LoginRequest
import com.garlicbread.foodbridge.dto.MealRequest
import com.garlicbread.foodbridge.dto.User
import com.garlicbread.foodbridge.dto.UserDetails
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/users/login")
    fun login(@Body userCredentials: LoginRequest): Call<User>

    @POST("/users/")
    fun signup(@Body userDetails: UserDetails): Call<User>

    @GET("/requirements/today/")
    fun getRequirements(): Call<List<MealRequest>>

    @GET("/community-centres/{id}")
    fun getCommunityCentres(@Path("id") id: String): Call<CommunityCentre>

    @POST("/food_items")
    fun sendDonation(@Body donationReq: DonationItem): Call<DonationItem>

    @GET("/users/{user_id}/token_count")
    fun getTokens(@Path("user_id") userId: String): Call<Int>
}
