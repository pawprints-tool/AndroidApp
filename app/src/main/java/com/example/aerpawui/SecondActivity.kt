package com.example.aerpawui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aerpawui.databinding.ActivitySecondBinding
import kotlin.math.roundToInt

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0

    lateinit var textInput: EditText

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIMER_EXTRA, 0.0)
            binding.timeTV.text = getTimeStringFromDouble(time)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textInput = findViewById(R.id.textInput)

        val imageButton2 = findViewById<ImageButton>(R.id.imageButton2)
        imageButton2.setOnClickListener {
            val Intent = Intent(this, ThirdActivity::class.java)
            startActivity(Intent)
        }

        val fourthScreenButton = findViewById<Button>(R.id.buttonMeasurements)
        fourthScreenButton.setOnClickListener {
            val Intent = Intent(this, FourthActivity::class.java)
            startActivity(Intent)
        }

        binding.imageButton3.setOnClickListener { startStopTimer() }
        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        enableEdgeToEdge()
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

    private fun resetTimer() {
        stopTimer()
        time = 0.0
        binding.timeTV.text = getTimeStringFromDouble(time)
    }

    private fun startStopTimer() {
        if (timerStarted) {
            stopTimer()
            binding.imageButton3.setImageResource(R.drawable.start)
        }else {
            startTimer()
            binding.imageButton3.setImageResource(R.drawable.stop)
        }
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIMER_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        timerStarted = false
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)
}

