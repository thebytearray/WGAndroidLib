package com.nasahacker.wireguard.service
/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
interface ServiceListener {
    fun onStateBroadcast(
        state: String,
        duration: String,
        downloadSpeed: String,
        uploadSpeed: String
    )

    fun onVpnDisconnected()
}