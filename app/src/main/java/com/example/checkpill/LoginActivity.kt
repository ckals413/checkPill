package com.example.checkpill

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checkpill.auth.AuthSessionManager
import com.example.checkpill.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: AuthSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = AuthSessionManager(this)

        if (sessionManager.isLoggedIn()) {
            openHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val userName = binding.userNameEditText.text.toString().trim()
            val pin = binding.pinEditText.text.toString().trim()
            if (userName.isBlank() || pin.length < 4) {
                Toast.makeText(this, "이름과 4자리 이상 PIN을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sessionManager.login(userName, pin)
            openHome()
        }
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
