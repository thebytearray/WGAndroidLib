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
 * ServiceManager
 *
 * Manages VPN-related services and permissions.
 *
 * @author Tamim Hossain
 * @contact tamimh.dev@gmail.com
 */
object ServiceManager {

    private var vpnPermissionLauncher: ActivityResultLauncher<Intent>? = null
    var notificationIconResId: Int = 0

    /**
     * Initializes the VPN Service Manager.
     *
     * @param activity The calling activity.
     * @param notificationIcon The resource ID for the notification icon.
     */
    fun init(activity: AppCompatActivity, notificationIcon: Int) {
        notificationIconResId = notificationIcon
        vpnPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Handle activity result
        }
    }

    /**
     * Checks if the device is ready for VPN connection.
     *
     * @param context The application context.
     * @return True if VPN permissions are granted, otherwise false.
     */
    fun isVpnReady(context: Context): Boolean {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val vpnPrepared = VpnService.prepare(context) == null
        return hasNotificationPermission && vpnPrepared
    }

    /**
     * Requests necessary permissions and prepares the device for VPN connection.
     *
     * @param activity The calling activity.
     */
    fun requestVpnPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 101)
            }
        }
        VpnService.prepare(activity)?.let { vpnIntent ->
            vpnPermissionLauncher?.launch(vpnIntent)
        }
    }

    /**
     * Starts the VPN tunnel service with the given configuration.
     *
     * @param context The application context.
     * @param config The VPN tunnel configuration.
     * @param blockedApps List of blocked apps (optional).
     */
    fun startVpnTunnel(context: Context, config: TunnelConfig, blockedApps: List<String>?) {
        val startIntent = Intent(context, TunnelService::class.java).apply {
            putExtra(BLOCKED_APPS, ArrayList(blockedApps ?: emptyList()))
            putExtra(TUNNEL_CONFIG, config)
            putExtra("NOTIFICATION_ICON", notificationIconResId)
        }
        startService(context, startIntent)
    }

    /**
     * Stops the active VPN tunnel service.
     *
     * @param context The application context.
     */
    fun stopVpnTunnel(context: Context) {
        val stopIntent = Intent(context, TunnelService::class.java).apply {
            action = STOP_ACTION
        }
        startService(context, stopIntent)
    }

    /**
     * Starts the service as a foreground service or a normal service depending on Android version.
     *
     * @param context The application context.
     * @param intent The intent to start the service.
     */
    private fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
