package org.thebytearray.wireguard.model


import com.wireguard.android.backend.Tunnel
import org.thebytearray.wireguard.util.Constants

/**
 * TheByteArray
 *
 * @developer Tamim Hossain
 * @mail contact@thebytearray.org
 */
class WgTunnel : Tunnel {
    override fun getName(): String {
        return Constants.SESSION_NAME
    }

    override fun onStateChange(newState: Tunnel.State) {}
}