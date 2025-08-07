package org.thebytearray.wireguard.service

import android.content.Context

/**
 * TheByteArray
 *
 * @developer Tamim Hossain
 * @mail contact@thebytearray.org
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