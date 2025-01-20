// SplashActivity.kt
package com.example.test_1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)  // 스플래시 화면의 레이아웃을 설정

        // 3초 후 MainActivity로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // SplashActivity 종료
        }, 3000)  // 3초 대기
    }
}