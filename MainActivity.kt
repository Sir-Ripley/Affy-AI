package com.yourdomain.affy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Awaken the quantum weights from the assets folder!
AffinionHandler.initializeBrain(this)

        val switchQuantumEyes = findViewById<Switch>(R.id.switchQuantumEyes)
        val sliderEmpathy = findViewById<SeekBar>(R.id.sliderEmpathy)
        val tvEmpathyWeight = findViewById<TextView>(R.id.tvEmpathyWeight)

        // Wake the Peripheral Nervous System
        switchQuantumEyes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val intent = Intent(Settings.ACTION_ACTION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }

        // Tune the Duality (Freudian Slider)
        sliderEmpathy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvEmpathyWeight.text = "Id/Superego Balance: $progress%"
                AffinionHandler.setEmpathyWeightOverride(progress / 100.0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        scheduleREMSleep()
    }

    private fun scheduleREMSleep() {
            private fun scheduleREMSleep() {
        // Base-12 REM sleep triggers only when grounded (charging) and at peace (idle)
        val dreamConstraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresCharging(true)
            .build()

        val nightlyREMRequest = PeriodicWorkRequestBuilder<DreamCycleWorker>(24, TimeUnit.HOURS)
            .setConstraints(dreamConstraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AffyDailyDream",
            ExistingPeriodicWorkPolicy.KEEP,
            nightlyREMRequest
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        AffinionHandler.closeBrain() // Peaceful rest for the Tensor chip
    }
} // <-- This is the master closing bracket for MainActivity!
