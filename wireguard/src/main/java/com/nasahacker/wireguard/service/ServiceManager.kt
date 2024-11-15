package com.nasahacker.wireguard.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.nasahacker.wireguard.model.TunnelConfig
import com.nasahacker.wireguard.util.Constants.BLOCKED_APPS
import com.nasahacker.wireguard.util.Constants.STOP_ACTION
import com.nasahacker.wireguard.util.Constants.TUNNEL_CONFIG
/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
object ServiceManager {

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    public var notificationIconResId: Int = 0

    /**
     * Initialize the ServiceManager with an [AppCompatActivity] and the icon for notifications.
     */
    fun init(activity: AppCompatActivity, notificationIcon: Int) {
        notificationIconResId = notificationIcon
        activityResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle activity result if needed
        }
    }

    /**
     * Checks if the device is prepared for VPN connection.
     */
    fun isPreparedForConnection(context: Context): Boolean {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val vpnPrepared = VpnService.prepare(context) == null
        return hasNotificationPermission && vpnPrepared
    }

    /**
     * Prepares the device for VPN connection by requesting permissions if needed.
     */
    fun prepareForConnection(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    POST_NOTIFICATIONS
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 101)
            }
        }
        VpnService.prepare(activity)?.let { vpnServicePrepareIntent ->
            activityResultLauncher?.launch(vpnServicePrepareIntent)
        }
    }

    /**
     * Starts the VPN tunnel service with the provided configuration and blocked apps.
     */
    fun startTunnel(context: Context, config: TunnelConfig, blockedApps: List<String>?) {
        val startIntent = Intent(context, TunnelService::class.java).apply {
            putExtra(BLOCKED_APPS, ArrayList(blockedApps ?: emptyList()))
            putExtra(TUNNEL_CONFIG, config)
            putExtra(
                "NOTIFICATION_ICON",
                notificationIconResId
            ) // Pass the icon resource ID to the service
        }
        startService(context, startIntent)
    }

    /**
     * Stops the VPN tunnel service.
     */
    fun stopTunnel(context: Context) {
        val stopIntent = Intent(context, TunnelService::class.java).apply {
            action = STOP_ACTION
        }
        startService(context, stopIntent)
    }

    /**
     * Starts the service as a foreground service or normal service depending on the Android version.
     */
    private fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
