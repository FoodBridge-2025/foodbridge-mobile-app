package com.garlicbread.foodbridge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.garlicbread.foodbridge.databinding.ActivityDonationFormBinding
import com.garlicbread.foodbridge.dto.DonationItem
import com.garlicbread.foodbridge.retrofit.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DonationForm : AppCompatActivity() {

    private lateinit var binding: ActivityDonationFormBinding
    private lateinit var photoURI: Uri
    private var flag = false
    var title = ""
    var desc = ""
    var quantity = 1
    var requestId = ""
    var userId = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentData = intent
        requestId = intentData.getStringExtra("RequestId").toString()

        val sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        userId = sharedPreferences.getString("id", "") ?: ""

        binding.slider.addOnChangeListener { _, value, _ ->
            binding.number.text = value.toInt().toString()
            quantity = value.toInt()
        }

        binding.btnUpload.setOnClickListener {
            openCamera()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(listOf(android.Manifest.permission.CAMERA).toTypedArray(), 1);
        }

        binding.donate.setOnClickListener {
            if (title.isNotEmpty() && flag) uploadImage()
        }

        binding.titleTextInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                title = s.toString().trim()
                if (title.isNotEmpty() && flag) {
                    binding.donate.alpha = 1f
                    binding.donate.isEnabled = true
                }
                else {
                    binding.donate.alpha = 0.4f
                    binding.donate.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        binding.descTextInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                desc = s.toString().trim()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

    }

    private fun openCamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        photoURI = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startForResult.launch(takePictureIntent)
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun encodeImageToBase64(context: Context, photoUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(photoUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP) // Convert to Base64
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    @SuppressLint("SetTextI18n")
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            flag = true
            photoURI.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                var bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap = bitmap.rotate(90f)
                binding.ivImagePreview.setImageBitmap(bitmap)

                val client = OkHttpClient()
                val photoBase64 = encodeImageToBase64(this, photoURI) ?: ""

                val requestBody = FormBody.Builder()
                    .add("image", photoBase64)
                    .build()

                val request = Request.Builder()
                    .url("https://3c4b-128-59-179-213.ngrok-free.app/upload_image")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()

                binding.insights.isVisible = true

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        response.body?.string()?.let { it1 ->
                            val json = JSONObject(it1)
                            val rating = json.getString("rating")
                            val reasoning = json.getString("reasoning")
                            runOnUiThread {
                                binding.insights.text = reasoning
                                binding.rating.isVisible = true
                                binding.value.text = "$rating (${rating.toInt() * 5} points)"
                                binding.value.isVisible = true
                            }
                        }
                    }
                })
            }

            if (title.isNotEmpty() && flag) {
                runOnUiThread {
                    binding.donate.alpha = 1f
                    binding.donate.isEnabled = true
                }
            }
            else {
                runOnUiThread {
                    binding.donate.alpha = 0.4f
                    binding.donate.isEnabled = false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateFormatted(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val ref = storageRef.child("images/${userId}/${getCurrentDateFormatted()}.jpg")
        val uploadTask = ref.putFile(photoURI)

        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                val downloadUri = uri.toString()

                RetrofitInstance.api.sendDonation(
                    DonationItem(
                        title,
                        desc,
                        downloadUri,
                        quantity,
                        userId,
                        requestId
                    )
                ).enqueue(object : Callback<DonationItem> {
                    override fun onResponse(call: Call<DonationItem>, response: Response<DonationItem>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@DonationForm, "Donation Request Sent !!!", Toast.LENGTH_SHORT).show()
                            val newIntent = Intent(this@DonationForm, Dashboard::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(newIntent)
                        }
                    }

                    override fun onFailure(call: Call<DonationItem>, t: Throwable) {
                        Toast.makeText(this@DonationForm, "Server down, please try again.", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

}