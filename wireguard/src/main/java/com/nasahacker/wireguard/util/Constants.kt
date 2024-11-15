package com.nasahacker.wireguard.util

import com.nasahacker.wireguard.model.TunnelState
/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
object Constants {
    /**
     * Notification related constants
     */
    const val CHANNEL_ID = "NETSS_CHANNEL"
    const val CHANNEL_NAME = "NETSS VPN background Service"
    const val FOREGROUND_ID = 1
    const val NOTIFICATION_TITLE = "NETSS VPN Connected"
    const val NOTIFICATION_TEXT = "Connected to NETSS Mode"
    const val SESSION_NAME = "Tunnel running"

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
    const val STATS_BROADCAST_ACTION = "NETSS_STATS_BROADCAST_ACTION"

    /**
     * Actions
     */
    const val STOP_ACTION = "STOP_ACTION"
    const val START_ACTION = "START_ACTION"
    const val BLOCKED_APPS = "BLOCKED_APPS"
    const val TUNNEL_CONFIG = "TUNNEL_CONFIG"
    const val SERVERS_LIST = "SERVERS_LIST"
    const val DISCONNECT_ACTION = "com.nasahacker.netss.DISCONNECT_ACTION"
}