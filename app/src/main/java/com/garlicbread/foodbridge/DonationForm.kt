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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.garlicbread.foodbridge.databinding.ActivityDonationFormBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class DonationForm : AppCompatActivity() {

    private lateinit var binding: ActivityDonationFormBinding
    private lateinit var photoURI: Uri
    private var flag = false
    var title = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var desc = ""
        var quantity = 0

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
            }
            if (title.isNotEmpty() && flag) {
                binding.donate.alpha = 1f
                binding.donate.isEnabled = true
            }
            else {
                binding.donate.alpha = 0.4f
                binding.donate.isEnabled = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateFormatted(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        return currentDate.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val ref = storageRef.child("images/${getCurrentDateFormatted()}.jpg")
        val uploadTask = ref.putFile(photoURI)

        uploadTask.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                val downloadUri = uri.toString()
                Toast.makeText(this, "Donation Request Sent !!!", Toast.LENGTH_SHORT).show()
                val newIntent = Intent(this, Dashboard::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(newIntent)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

}