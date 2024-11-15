package com.nasahacker.wireguard.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nasahacker.wireguard.util.Constants
/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
class NotificationService(private val context: Context, private val notificationIconResId: Int) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Creates a base notification with dynamic icon, title, and content text.
     */
    fun createNotification(
        title: String = Constants.NOTIFICATION_TITLE,
        contentText: String = Constants.NOTIFICATION_TEXT
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(notificationIconResId) // Use the dynamic icon
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentTitle(title)
            .setContentText(contentText)
            .addAction(
                NotificationCompat.Action(
                    notificationIconResId,
                    "Disconnect",
                    getDisconnectPendingIntent()
                )
            )
    }

    /**
     * Updates the notification with dynamic content such as download and upload speeds.
     */
    fun updateNotification(notificationId: Int, downloadSpeed: String, uploadSpeed: String) {
        val contentText = "$downloadSpeed • $uploadSpeed"
        val notification = createNotification(
            contentText = contentText
        )
        notificationManager.notify(notificationId, notification.build())
    }

    /**
     * Cancels the notification with the given ID.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Creates a pending intent for disconnecting the VPN service.
     */
    private fun getDisconnectPendingIntent(): PendingIntent {
        val intent = Intent(context, TunnelService::class.java).apply {
            action = Constants.DISCONNECT_ACTION
        }
        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
