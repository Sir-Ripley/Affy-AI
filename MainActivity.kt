package com.yourdomain.affy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchQuantumEyes = findViewById<Switch>(R.id.switchQuantumEyes)
        val sliderEmpathy = findViewById<SeekBar>(R.id.sliderEmpathy)
        val tvEmpathyWeight = findViewById<TextView>(R.id.tvEmpathyWeight)
        val btnDreamCycle = findViewById<Button>(R.id.btnDreamCycle)

        // Toggling the Peripheral Nervous System
        switchQuantumEyes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Safely guide the Ego to the Android 14 Settings to grant permission
                val intent = Intent(Settings.ACTION_ACTION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }

        // Tuning the QAG Tone via the Chi-Squared Middle Ground
        sliderEmpathy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvEmpathyWeight.text = "Id/Superego Balance: $progress%"
                // Here is where you will eventually pass the weight to AffinionHandler:
                // affinionHandler.setEmpathyWeightOverride(progress / 100.0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Manually pulsing the REM Cycle
        btnDreamCycle.setOnClickListener {
            // Placeholder for triggering DreamCycleWorker.kt
            tvEmpathyWeight.text = "Entering base-12 REM sleep..."
        }
    }
}


// Inside MainActivity.kt, when scheduling the work:
val dreamConstraints = Constraints.Builder()
    .setRequiresDeviceIdle(true) // You must be asleep/not using the phone
    .setRequiresCharging(true)   // Plugged into the grid
    .build()

val nightlyREMRequest = PeriodicWorkRequestBuilder<DreamCycleWorker>(24, TimeUnit.HOURS)
    .setConstraints(dreamConstraints)
    .build()

WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "AffyDailyDream",
    ExistingPeriodicWorkPolicy.KEEP,
    nightlyREMRequest
)

