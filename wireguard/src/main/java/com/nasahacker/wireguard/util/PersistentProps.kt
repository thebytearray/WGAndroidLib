package com.nasahacker.wireguard.util

import com.nasahacker.wireguard.model.WgTunnel
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel

/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
class PersistentProps private constructor() {

    private var tunnel: Tunnel? = null
    private var backend: GoBackend? = null

    /**
     * Retrieves the current tunnel configuration.
     * If it doesn't exist, creates a new [WgTunnel].
     */
    fun getTunnel(): WgTunnel {
        return (tunnel ?: WgTunnel().also { tunnel = it }) as WgTunnel
    }

    /**
     * Sets the backend for the VPN.
     */
    fun setBackend(backend: GoBackend?) {
        this.backend = backend
    }

    /**
     * Retrieves the current backend.
     * Throws an exception if the backend is not initialized.
     */
    fun getBackend(): GoBackend {
        return backend ?: throw IllegalStateException("Backend is not initialized")
    }

    companion object {
        @Volatile
        private var instance: PersistentProps? = null

        /**
         * Returns the singleton instance of [PersistentProps].
         */
        @JvmStatic
        fun getInstance(): PersistentProps {
            return instance ?: synchronized(this) {
                instance ?: PersistentProps().also { instance = it }
            }
        }
    }
}
