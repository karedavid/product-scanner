package com.kdapps.productscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kdapps.productscanner.databinding.ActivityImageViewerBinding

import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.extras?.getString("image_uri")

        val storageReference = Firebase.storage("gs://product-scanner-7093a.appspot.com").reference

        if (imageUri != null && Firebase.auth.currentUser != null) {
            GlideApp.with(this)
                .load(storageReference.child(Firebase.auth.currentUser!!.uid).child(imageUri))
                .into(binding.productIw)
        }


    }
}