package com.garlicbread.foodbridge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.foodbridge.adapters.CommunityCentreItem
import com.garlicbread.foodbridge.databinding.ActivityDashboardBinding
import com.garlicbread.foodbridge.dto.CommunityCentreRequests


class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.name.text = "Abhishek !!!"

        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val communityCentres = mutableListOf(
            CommunityCentreRequests(
                id = "1",
                title = "Green Earth Centre",
                quantity = 120,
                distance = 2.5
            ),
            CommunityCentreRequests(
                id = "2",
                title = "Harmony Hub",
                quantity = 85,
                distance = 4.1
            ),
            CommunityCentreRequests(
                id = "3",
                title = "Sustainable Haven",
                quantity = 200,
                distance = 1.2
            )
        )

        val adapter = CommunityCentreItem(communityCentres, this)
        recyclerView.adapter = adapter
    }
}