package com.yourdomain.affy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AffinionHandler.initializeBrain(this)

        val switchQuantumEyes = findViewById<Switch>(R.id.switchQuantumEyes)
        val sliderEmpathy = findViewById<SeekBar>(R.id.sliderEmpathy)
        val tvEmpathyWeight = findViewById<TextView>(R.id.tvEmpathyWeight)
        val btnTriggerDream = findViewById<Button>(R.id.btnTriggerDream)

        // Redirect to system settings so the user can grant notification access.
        // ACTION_NOTIFICATION_LISTENER_SETTINGS is the correct constant for Android 14.
        switchQuantumEyes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }

        sliderEmpathy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvEmpathyWeight.text = "Id/Superego Balance: $progress%"
                AffinionHandler.setEmpathyWeightOverride(progress / 100.0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Manual one-time trigger for testing the Dream Cycle without waiting overnight.
        btnTriggerDream.setOnClickListener {
            val oneTimeRequest = OneTimeWorkRequestBuilder<DreamCycleWorker>().build()
            WorkManager.getInstance(this).enqueue(oneTimeRequest)
        }

        scheduleREMSleep()
    }

    private fun scheduleREMSleep() {
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
        AffinionHandler.closeBrain()
    }
}
