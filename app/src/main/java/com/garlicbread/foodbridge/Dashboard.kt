package com.garlicbread.foodbridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.foodbridge.adapters.CommunityCentreItem
import com.garlicbread.foodbridge.databinding.ActivityDashboardBinding
import com.garlicbread.foodbridge.dto.MealRequest
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        val username = sharedPreferences.getString("name", "User") ?: "User"

        binding.name.text = username

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(), 1);
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation(fusedLocationClient)

        binding.coins.setOnClickListener {
            val newIntent = Intent(this, PointsActivity::class.java)
            startActivity(newIntent)
        }

        binding.signOut.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            Toast.makeText(
                this,
                "Signed out successfully",
                Toast.LENGTH_LONG
            ).show()
            val newIntent = Intent(this, LoginActivity::class.java)
            newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(newIntent)
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distanceMeters = R * c
        val distanceMiles = distanceMeters / 1609.34

        return "%.2f".format(distanceMiles).toDouble()
    }

    private fun getCurrentLocation(fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                CoroutineScope(Dispatchers.IO).launch {
                    RetrofitInstance.api.getRequirements().enqueue(object : Callback<List<MealRequest>> {
                        override fun onResponse(call: Call<List<MealRequest>>, response: Response<List<MealRequest>>) {
                            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                                val items = response.body()!!.sortedBy { request ->
                                    haversine(latitude, longitude, request.community_centre.latitude, request.community_centre.longitude)
                                }

                                val listItems = items.map {
                                    com.garlicbread.foodbridge.dto.CommunityCentreItem(
                                        it.community_centre.id,
                                        it.community_centre.name,
                                        it.servings,
                                        haversine(latitude, longitude, it.community_centre.latitude, it.community_centre.longitude),
                                        it.id
                                    )
                                }

                                val adapter = CommunityCentreItem(listItems, this@Dashboard)
                                val recyclerView = findViewById<RecyclerView>(R.id.list)
                                recyclerView.layoutManager = LinearLayoutManager(this@Dashboard)
                                recyclerView.adapter = adapter
                            } else {
                               //
                            }
                        }

                        override fun onFailure(call: Call<List<MealRequest>>, t: Throwable) {
                            Toast.makeText(this@Dashboard, "Server down, please try again.", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }
}