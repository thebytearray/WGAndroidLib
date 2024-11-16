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
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
class TunnelService : Service(), CoroutineScope {

    private lateinit var notificationManager: NotificationService
    private lateinit var backend: Backend
    private lateinit var tunnel: Tunnel
    private var listener: ServiceListener? = null

    private var seconds: Long = 0
    private var lastRxBytes: Long = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: 0
    private var lastTxBytes: Long = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: 0
    private var isRunning: Boolean = false
    private var state: TunnelState = TunnelState.DISCONNECTED
    private var job: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationService(this, ServiceManager.notificationIconResId)
        initializeBackend()
        startForeground(Constants.FOREGROUND_ID, notificationManager.createNotification().build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.STOP_ACTION, Constants.DISCONNECT_ACTION -> {
                    handleStopOrDisconnect()
                    return START_NOT_STICKY
                }

                else -> handleStart(intent)
            }
        }
        return START_STICKY
    }

    private fun handleStopOrDisconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            stopVPN()
            stopSelf()
        }
    }

    private fun handleStart(intent: Intent) {
        val blockedApplications =
            intent.getStringArrayListExtra(Constants.BLOCKED_APPS) ?: emptyList()
        if (!isRunning) {
            val config = intent.getSerializableExtra(Constants.TUNNEL_CONFIG) as? TunnelConfig
            config?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    startVPN(it, blockedApplications)
                }
            }
        } else {
            job = launch(Dispatchers.IO) {
                stopVPN()
            }
        }
    }

    private fun initializeBackend() {
        PersistentProps.getInstance().setBackend(GoBackend(this))
        backend = PersistentProps.getInstance().getBackend()
        tunnel = PersistentProps.getInstance().getTunnel()
    }

    private fun stopVPN() {
        try {
            if (backend.getState(tunnel) == Tunnel.State.UP) {
                backend.setState(tunnel, Tunnel.State.DOWN, null)
                stopTimer()
                sendDisconnectBroadcast()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVPN(tunnelModel: TunnelConfig, blockedApplications: List<String>) {
        Log.d("TAG", "startVPN: $tunnelModel")
        state = TunnelState.CONNECTING

        val interfaceBuilder = Interface.Builder().apply {
            addAddress(InetNetwork.parse(tunnelModel.interfaceField.address))
                .excludeApplications(blockedApplications)
            parsePrivateKey(tunnelModel.interfaceField.privateKey)
            setMtu(1420)
        }

        val peerBuilder = Peer.Builder().apply {
            tunnelModel.peer.allowedIps.forEach {
                addAllowedIp(InetNetwork.parse(it.trim()))
            }
            setEndpoint(InetEndpoint.parse(tunnelModel.peer.endpoint))
            parsePublicKey(tunnelModel.peer.publicKey)
        }

        try {
            val config = Config.Builder()
                .setInterface(interfaceBuilder.build())
                .addPeer(peerBuilder.build())
                .build()

            backend.setState(tunnel, Tunnel.State.UP, config)
            state = TunnelState.CONNECTED
            isRunning = true
            startTimer()
            broadcastState()
        } catch (e: Exception) {
            state = TunnelState.DISCONNECTED
            e.printStackTrace()
        }
    }

    private fun broadcastState() {
        val currentRxBytes = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: lastRxBytes
        val currentTxBytes = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: lastTxBytes

        state = try {
            if (backend.getState(tunnel) == Tunnel.State.UP) TunnelState.CONNECTED
            else TunnelState.DISCONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
            TunnelState.DISCONNECTED
        }

        val duration = seconds.formatDuration()
        val downloadSpeed = "↓${(currentRxBytes - lastRxBytes).toSpeedString()}"
        val uploadSpeed = "↑${(currentTxBytes - lastTxBytes).toSpeedString()}"

        listener?.onStateBroadcast(this,state.toString(), duration, downloadSpeed, uploadSpeed)
        notificationManager.updateNotification(Constants.FOREGROUND_ID, downloadSpeed, uploadSpeed)

        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
    }

    private fun sendDisconnectBroadcast() {
        listener?.onVpnDisconnected()
    }

    private fun stopTimer() {
        seconds = 0
        broadcastState()
    }

    private fun startTimer() {
        job = launch(Dispatchers.IO) {
            while (isRunning) {
                seconds++
                broadcastState()
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        stopVPN()
        job?.cancel()
        notificationManager.cancelNotification(Constants.FOREGROUND_ID)
        super.onDestroy()
    }
}
