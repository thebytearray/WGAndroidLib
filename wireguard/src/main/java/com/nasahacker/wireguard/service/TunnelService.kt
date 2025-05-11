package com.nasahacker.wireguard.service

import android.app.Service
import android.content.Intent
import android.net.TrafficStats
import android.os.IBinder
import android.util.Log
import com.nasahacker.wireguard.extension.formatDuration
import com.nasahacker.wireguard.extension.toSpeedString
import com.nasahacker.wireguard.model.TunnelConfig
import com.nasahacker.wireguard.model.TunnelState
import com.nasahacker.wireguard.util.Constants
import com.nasahacker.wireguard.util.PersistentProps
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * VpnTunnelService
 *
 * Handles the VPN tunnel lifecycle and state management.
 * This service manages the WireGuard VPN connection, including connection state,
 * traffic statistics, and notification updates.
 *
 * @author Tamim Hossain
 * @contact tamimh.dev@gmail.com
 */
class TunnelService : Service(), CoroutineScope {
    private val TAG = "TunnelService"

    private lateinit var vpnNotificationManager: NotificationService
    private lateinit var backend: Backend
    private lateinit var tunnel: Tunnel
    private var vpnListener: ServiceListener? = null

    private var uptimeSeconds: Long = 0
    private var lastRxBytes: Long = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: 0
    private var lastTxBytes: Long = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: 0
    private var isVpnActive: Boolean = false
    private var vpnState: TunnelState = TunnelState.DISCONNECTED
    private var vpnJob: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            vpnNotificationManager = NotificationService(this, ServiceManager.notificationIconResId)
            initializeBackend()
            startForeground(Constants.FOREGROUND_ID, vpnNotificationManager.createNotification().build())
            Log.d(TAG, "TunnelService created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create TunnelService", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            try {
                when (it.action) {
                    Constants.STOP_ACTION, Constants.DISCONNECT_ACTION -> {
                        handleVpnStopOrDisconnect()
                        return START_NOT_STICKY
                    }
                    else -> handleVpnStart(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling intent action: ${it.action}", e)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    /**
     * Handles VPN stop or disconnect requests.
     */
    private fun handleVpnStopOrDisconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                disconnectVpn()
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping VPN", e)
                stopSelf()
            }
        }
    }

    /**
     * Handles VPN start requests.
     */
    private fun handleVpnStart(intent: Intent) {
        val blockedApps = intent.getStringArrayListExtra(Constants.BLOCKED_APPS) ?: emptyList()
        if (!isVpnActive) {
            val config = intent.getSerializableExtra(Constants.TUNNEL_CONFIG) as? TunnelConfig
            if (config != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        connectVpn(config, blockedApps)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error connecting VPN", e)
                        stopSelf()
                    }
                }
            } else {
                Log.e(TAG, "Invalid tunnel configuration")
                stopSelf()
            }
        } else {
            vpnJob = launch(Dispatchers.IO) {
                try {
                    disconnectVpn()
                } catch (e: Exception) {
                    Log.e(TAG, "Error disconnecting VPN", e)
                    stopSelf()
                }
            }
        }
    }

    /**
     * Initializes the VPN backend.
     */
    private fun initializeBackend() {
        try {
            PersistentProps.getInstance().setBackend(GoBackend(this))
            backend = PersistentProps.getInstance().getBackend()
            tunnel = PersistentProps.getInstance().getTunnel()
            Log.d(TAG, "Backend initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize backend", e)
            throw e
        }
    }

    /**
     * Disconnects the VPN tunnel.
     */
    private fun disconnectVpn() {
        try {
            if (backend.getState(tunnel) == Tunnel.State.UP) {
                backend.setState(tunnel, Tunnel.State.DOWN, null)
                stopVpnTimer()
                sendDisconnectBroadcast()
                Log.d(TAG, "VPN disconnected successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting VPN", e)
            throw e
        }
    }

    /**
     * Establishes the VPN tunnel.
     */
    private suspend fun connectVpn(config: TunnelConfig, blockedApps: List<String>) {
        Log.d(TAG, "Connecting to VPN: $config")
        vpnState = TunnelState.CONNECTING

        try {
            val interfaceBuilder = Interface.Builder().apply {
                addAddress(InetNetwork.parse(config.interfaceField.address))
                    .excludeApplications(blockedApps)
                parsePrivateKey(config.interfaceField.privateKey)
                setMtu(1420)
            }

            val peerBuilder = Peer.Builder().apply {
                config.peer.allowedIps.forEach { addAllowedIp(InetNetwork.parse(it.trim())) }
                setEndpoint(InetEndpoint.parse(config.peer.endpoint))
                parsePublicKey(config.peer.publicKey)
            }

            val vpnConfig = Config.Builder()
                .setInterface(interfaceBuilder.build())
                .addPeer(peerBuilder.build())
                .build()

            backend.setState(tunnel, Tunnel.State.UP, vpnConfig)
            vpnState = TunnelState.CONNECTED
            isVpnActive = true
            startVpnTimer()
            broadcastVpnState()
            Log.d(TAG, "VPN connected successfully")
        } catch (e: Exception) {
            vpnState = TunnelState.DISCONNECTED
            Log.e(TAG, "Error connecting VPN", e)
            throw e
        }
    }

    /**
     * Broadcasts the current VPN state.
     */
    private fun broadcastVpnState() {
        try {
            val currentRxBytes = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: lastRxBytes
            val currentTxBytes = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: lastTxBytes

            vpnState = try {
                if (backend.getState(tunnel) == Tunnel.State.UP) TunnelState.CONNECTED
                else TunnelState.DISCONNECTED
            } catch (e: Exception) {
                Log.e(TAG, "Error getting tunnel state", e)
                TunnelState.DISCONNECTED
            }

            val duration = uptimeSeconds.formatDuration()
            val downloadSpeed = "↓${(currentRxBytes - lastRxBytes).toSpeedString()}"
            val uploadSpeed = "↑${(currentTxBytes - lastTxBytes).toSpeedString()}"

            vpnListener?.onStateBroadcast(this, vpnState.toString(), duration, downloadSpeed, uploadSpeed)
            vpnNotificationManager.updateNotification(Constants.FOREGROUND_ID, downloadSpeed, uploadSpeed)

            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting VPN state", e)
        }
    }

    /**
     * Sends a disconnect broadcast.
     */
    private fun sendDisconnectBroadcast() {
        try {
            vpnListener?.onVpnDisconnected()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending disconnect broadcast", e)
        }
    }

    /**
     * Stops the VPN uptime timer.
     */
    private fun stopVpnTimer() {
        try {
            uptimeSeconds = 0
            broadcastVpnState()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN timer", e)
        }
    }

    /**
     * Starts the VPN uptime timer.
     */
    private fun startVpnTimer() {
        vpnJob = launch(Dispatchers.IO) {
            try {
                while (isVpnActive) {
                    uptimeSeconds++
                    broadcastVpnState()
                    delay(1000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in VPN timer", e)
            }
        }
    }

    override fun onDestroy() {
        try {
            disconnectVpn()
            vpnJob?.cancel()
            vpnNotificationManager.cancelNotification(Constants.FOREGROUND_ID)
            Log.d(TAG, "TunnelService destroyed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying TunnelService", e)
        } finally {
            super.onDestroy()
        }
    }
}
