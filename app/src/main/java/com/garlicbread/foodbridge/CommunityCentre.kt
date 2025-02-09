package com.garlicbread.foodbridge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garlicbread.foodbridge.databinding.ActivityCommunityCentreBinding
import com.garlicbread.foodbridge.dto.CommunityCentre
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityCentre : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var id: String
    private lateinit var reqId: String
    private lateinit var binding: ActivityCommunityCentreBinding
    private var latitude = 1000.0
    private var longitude = 1000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunityCentreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentData = intent
        id = intentData.getStringExtra("CommunityCentreId").toString()
        reqId = intentData.getStringExtra("RequestId").toString()

        binding.phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.setData(Uri.parse("tel:${formatNumber(binding.number.text.toString())}"))
            startActivity(intent)
        }

        binding.location.setOnClickListener {
            if (latitude != 1000.0 && longitude != 1000.0) {
                val uri = "http://maps.google.com/maps?z=17&q=$latitude,$longitude(${binding.title.text})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }
        }

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.donate.setOnClickListener {
            val newIntent = Intent(this, DonationForm::class.java)
            newIntent.putExtra("RequestId", reqId)
            startActivity(newIntent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        if (id.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                RetrofitInstance.api.getCommunityCentres(id)
                    .enqueue(object : Callback<CommunityCentre> {
                        override fun onResponse(
                            call: Call<CommunityCentre>,
                            response: Response<CommunityCentre>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                binding.title.text = response.body()!!.name
                                binding.number.text = response.body()!!.contact
                                binding.address.text = response.body()!!.address

                                val location = LatLng(response.body()!!.latitude, response.body()!!.longitude)
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

                                latitude = response.body()!!.latitude
                                longitude = response.body()!!.longitude

                                map.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title(response.body()!!.name)
                                )
                            }
                        }

                        override fun onFailure(call: Call<CommunityCentre>, t: Throwable) {
                            Toast.makeText(
                                this@CommunityCentre,
                                "Server down, please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }
    }

    private fun formatNumber(number: String): String {
        return number.toCharArray().filter { it.isDigit() }.joinToString("")
    }
}