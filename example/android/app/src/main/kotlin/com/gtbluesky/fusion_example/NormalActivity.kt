package com.gtbluesky.fusion_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gtbluesky.fusion_example.databinding.ActivityABinding

class NormalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityABinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityABinding.inflate(layoutInflater)
        setContentView(binding.root)
        val title = intent.getStringExtra("title")
        binding.tvA.text = title
    }
}