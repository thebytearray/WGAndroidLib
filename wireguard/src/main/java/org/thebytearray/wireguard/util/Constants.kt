package org.thebytearray.wireguard.util

import org.thebytearray.wireguard.model.TunnelState


/**
 * TheByteArray
 *
 * @developer Tamim Hossain
 * @mail contact@thebytearray.org
 */
object Constants {
    /**
     * Notification related constants
     */
    const val CHANNEL_ID = "WIREGUARD_CHANNEL"
    const val CHANNEL_NAME = "WireGuard Background Service"
    const val FOREGROUND_ID = 1
    const val NOTIFICATION_TITLE = "WireGuard Connected"
    const val NOTIFICATION_TEXT = "Connected to WireGuard Tunnel"
    const val SESSION_NAME = "WireGuard Tunnel"

    /**
     * Intent related constants
     */
    const val STATE = "STATE"
    val DEFAULT_STATE = TunnelState.DISCONNECTED
    const val DURATION = "DURATION"
    const val DEFAULT_DURATION = "00:00:00"
    const val DOWNLOAD_SPEED = "DOWNLOAD_SPEED"
    const val DEFAULT_DOWNLOAD_SPEED = "↓ 00 b/s"
    const val UPLOAD_SPEED = "UPLOAD_SPEED"
    const val DEFAULT_UPLOAD_SPEED = "↑ 00 b/s"
    const val STATS_BROADCAST_ACTION = "WIREGUARD_STATS_BROADCAST_ACTION"

    /**
     * Actions
     */
    const val STOP_ACTION = "STOP_ACTION"
    const val START_ACTION = "START_ACTION"
    const val BLOCKED_APPS = "BLOCKED_APPS"
    const val TUNNEL_CONFIG = "TUNNEL_CONFIG"
    const val SERVERS_LIST = "SERVERS_LIST"
    const val DISCONNECT_ACTION = "WIREGUARD_DISCONNECT_ACTION"
}
