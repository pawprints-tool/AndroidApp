package com.example.aerpawui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

class FourthActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private var time = 0.0

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIMER_EXTRA, 0.0)
            timeTextView.text = getTimeStringFromDouble(time)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fourth)

        val secondScreenButton = findViewById<Button>(R.id.buttonLaunch)
        secondScreenButton.setOnClickListener {
            val Intent = Intent(this, SecondActivity::class.java)
            startActivity(Intent)
        }

        // 🔹 TextView that shows the timer
        timeTextView = findViewById(R.id.timeTVFourth)

        val imageButton3 = findViewById<ImageButton>(R.id.imageButton3)
        imageButton3.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Listen to the timer service
        registerReceiver(
            updateTime,
            IntentFilter(TimerService.TIMER_UPDATED)
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateTime)
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 3600 / 60
        val seconds = resultInt % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
