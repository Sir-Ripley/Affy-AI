package com.yourdomain.affy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load TFLite models on an IO thread — blocking file I/O must not run on the main thread
        lifecycleScope.launch(Dispatchers.IO) {
            AffinionHandler.initializeBrain(applicationContext)
        }

        val switchQuantumEyes = findViewById<Switch>(R.id.switchQuantumEyes)
        val sliderEmpathy = findViewById<SeekBar>(R.id.sliderEmpathy)
        val tvEmpathyWeight = findViewById<TextView>(R.id.tvEmpathyWeight)
        val btnTriggerDream = findViewById<Button>(R.id.btnTriggerDream)

        // Reflect current notification permission state on the switch
        switchQuantumEyes.isChecked = QuantumEyesService.checkNotificationPermission(this)

        switchQuantumEyes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !QuantumEyesService.checkNotificationPermission(this)) {
                // Redirect to system settings — Android 14 requires explicit user grant
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                // Revert the switch; it will reflect the real state when the user returns
                switchQuantumEyes.isChecked = false
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

        // Manual Dream Cycle trigger for testing — runs immediately, ignoring idle/charging constraints
        btnTriggerDream.setOnClickListener {
            val testRequest = OneTimeWorkRequestBuilder<DreamCycleWorker>().build()
            WorkManager.getInstance(this).enqueue(testRequest)
            Toast.makeText(this, "Dream Cycle triggered manually.", Toast.LENGTH_SHORT).show()
        }

        scheduleREMSleep()
    }

    /**
     * Schedules the nightly REM consolidation via WorkManager.
     * Runs every 24 hours, only when the device is idle and charging.
     * KEEP policy prevents duplicate scheduling on Activity restarts.
     */
    private fun scheduleREMSleep() {
        val dreamConstraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresCharging(true)
            .build()

        val nightlyRequest = PeriodicWorkRequestBuilder<DreamCycleWorker>(24, TimeUnit.HOURS)
            .setConstraints(dreamConstraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DreamCycleWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            nightlyRequest
        )
    }

    override fun onResume() {
        super.onResume()
        // Re-sync the switch after the user returns from the Settings screen
        val switchQuantumEyes = findViewById<Switch>(R.id.switchQuantumEyes)
        switchQuantumEyes.isChecked = QuantumEyesService.checkNotificationPermission(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AffinionHandler.closeBrain()
    }
}
