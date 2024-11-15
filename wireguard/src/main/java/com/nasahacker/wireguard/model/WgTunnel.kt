package com.nasahacker.wireguard.model

import com.nasahacker.wireguard.util.Constants
import com.wireguard.android.backend.Tunnel

/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
class WgTunnel : Tunnel
{
    override fun getName(): String
    {
        return Constants.SESSION_NAME
    }

    override fun onStateChange(newState: Tunnel.State?)
    {
    }
}