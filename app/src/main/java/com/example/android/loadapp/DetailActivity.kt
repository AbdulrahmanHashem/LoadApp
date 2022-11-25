package com.example.android.loadapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.persistableBundleOf
import com.example.android.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layout.ok.setOnClickListener {
            System.exit(0)
        }

        binding.layout.downloadable.text = intent.getBundleExtra("Download Info")?.getString("File Name")
        binding.layout.downloadStatus.text = intent.getBundleExtra("Download Info")?.getString("Status")
    }
}
