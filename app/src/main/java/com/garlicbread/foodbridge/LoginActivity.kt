package com.garlicbread.foodbridge

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garlicbread.foodbridge.databinding.ActivityLoginBinding
import com.garlicbread.foodbridge.dto.LoginRequest
import com.garlicbread.foodbridge.dto.User
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        val id = sharedPreferences.getString("id", "") ?: ""

        if (id.isNotBlank()) {
            val newIntent = Intent(this, Dashboard::class.java)
            startActivity(newIntent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.trim().toString()
            val password = binding.etPassword.text.trim().toString()

            if (email.isNotBlank() && password.isNotBlank()) login(email, password)
        }

        binding.btnSignUp.setOnClickListener {
            val newIntent = Intent(this, SignUpActivity::class.java)
            startActivity(newIntent)
        }
    }

    private fun login(email: String, password: String) {
        RetrofitInstance.api.login(
            LoginRequest(
                email,
                password
            )
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()
                        ?.let { saveDetails(email, password, it.user_id.id, it.user_id.name) }
                    val newIntent = Intent(this@LoginActivity, Dashboard::class.java)
                    startActivity(newIntent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid Credentials !!!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Server down, please try again.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveDetails(
        email: String,
        password: String,
        id: String,
        name: String
    ) {
        val prefs = this.getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        prefs.edit().putString("email", email).apply()
        prefs.edit().putString("password", password).apply()
        prefs.edit().putString("id", id).apply()
        prefs.edit().putString("name", name).apply()
    }
}