package com.garlicbread.foodbridge

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garlicbread.foodbridge.databinding.ActivityPointsBinding
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PointsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPointsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        binding = ActivityPointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", "") ?: ""

        RetrofitInstance.api.getTokens(userId).enqueue(object : Callback<Int> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    binding.number.text = response.body()?.toString() ?: "0"
                    binding.status.text = "${response.body()?.toString()} / 5000 points"
                    binding.progressBar.progress = ((response.body()!! / 5000.0f) * 100).toInt()
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(
                    this@PointsActivity,
                    "Server down, please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }
}