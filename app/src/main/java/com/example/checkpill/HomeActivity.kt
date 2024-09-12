package com.example.checkpill

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.checkpill.databinding.ActivityHomeBinding
import com.example.checkpill.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}