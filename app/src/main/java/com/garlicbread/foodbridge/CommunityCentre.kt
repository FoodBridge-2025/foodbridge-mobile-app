package com.garlicbread.foodbridge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.garlicbread.foodbridge.databinding.ActivityCommunityCentreBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CommunityCentre : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCommunityCentreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.text = "Test Community Centre"
        binding.number.text = "1234556789"
        binding.address.text = "Test address"

        binding.phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.setData(Uri.parse("tel:${formatNumber("123456789")}"))
            startActivity(intent)
        }

        binding.location.setOnClickListener {
            val latitude = "12.3"
            val longitude = "88"

            val uri = "http://maps.google.com/maps?z=17&q=$latitude,$longitude(${"organisation.name"})"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.donate.setOnClickListener {
            val newIntent = Intent(this, DonationForm::class.java)
            newIntent.putExtra("CommunityCentreId", "id")
            startActivity(newIntent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val location = LatLng(40.7128, -74.0060) // New York
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))

        map.addMarker(
            MarkerOptions()
                .position(location)
                .title("My Custom Marker")
        )
    }

    private fun formatNumber(number: String): String {
        return number.toCharArray().filter { it.isDigit() }.joinToString("")
    }
}