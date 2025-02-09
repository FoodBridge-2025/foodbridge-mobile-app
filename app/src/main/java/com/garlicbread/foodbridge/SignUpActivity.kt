package com.garlicbread.foodbridge

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garlicbread.foodbridge.databinding.ActivitySignUpBinding
import com.garlicbread.foodbridge.dto.User
import com.garlicbread.foodbridge.dto.UserDetails
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogon.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotBlank() && address.isNotBlank() && email.isNotBlank() && password.isNotBlank() && phone.isNotBlank()) {
                signUp(name, address, phone, email, password)
            }
        }

    }

    private fun signUp(name: String, address: String, phone: String, email: String, password: String) {
        RetrofitInstance.api.signup(
            UserDetails(
                name,
                address,
                phone,
                email,
                password
            )
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()
                        ?.let { saveDetails(it.user_id.id, name, email, password) }
                    val newIntent = Intent(this@SignUpActivity, Dashboard::class.java)
                    newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(newIntent)
                } else {
                    Toast.makeText(this@SignUpActivity,
                        response.errorBody()?.string()
                            ?: "Unknown error occurred. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Server down, please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun saveDetails(
        id: String,
        name: String,
        email: String,
        password: String,
    ) {
        val prefs = this.getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        prefs.edit().putString("email", email).apply()
        prefs.edit().putString("password", password).apply()
        prefs.edit().putString("id", id).apply()
        prefs.edit().putString("name", name).apply()
    }
}