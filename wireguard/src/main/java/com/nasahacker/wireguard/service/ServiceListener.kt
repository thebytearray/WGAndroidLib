package com.nasahacker.wireguard.service

import android.content.Context

/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
interface ServiceListener {
    fun onStateBroadcast(
        context: Context,
        state: String,
        duration: String,
        downloadSpeed: String,
        uploadSpeed: String
    )

    fun onVpnDisconnected()
}