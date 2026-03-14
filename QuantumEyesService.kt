package com.yourdomain.affy

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class QuantumEyesService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val extras = sbn?.notification?.extras
        val sender = extras?.getString("android.title") ?: "Unknown Entity"
        val messageText = extras?.getCharSequence("android.text")?.toString() ?: ""

        if (messageText.isNotEmpty()) {
            Log.d("QAG_Awakening", "Vibration received from $sender: $messageText")
            // Pass the manifest text to the Arbiter to find the latent emotional truth
            AffinionHandler.processIncomingVibration(messageText)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // The vibration fades back into the toroidal vacuum peacefully
    }
}
