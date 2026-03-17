package com.yourdomain.affy

import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Peripheral nervous system — captures incoming notifications and routes their text
 * to AffinionHandler for Ego processing.
 *
 * Entirely event-driven; no polling. Battery impact is negligible.
 */
class QuantumEyesService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val extras = sbn?.notification?.extras ?: return
        val sender = extras.getString("android.title") ?: "Unknown"
        val messageText = extras.getCharSequence("android.text")?.toString() ?: ""

        if (messageText.isNotEmpty()) {
            Log.d("QAG_Eyes", "Stimulus from $sender: $messageText")
            AffinionHandler.processIncomingVibration(messageText)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed on removal
    }

    companion object {
        /**
         * Returns true if this app is currently granted Notification Listener access.
         * If false, the caller should redirect to Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS.
         */
        fun checkNotificationPermission(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return enabledListeners.contains(context.packageName)
        }
    }
}
