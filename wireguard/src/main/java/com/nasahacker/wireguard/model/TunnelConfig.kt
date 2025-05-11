package com.nasahacker.wireguard.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.net.InetAddress
import java.util.regex.Pattern

/**
 * Configuration class for WireGuard tunnel.
 * This class represents the configuration needed to establish a WireGuard VPN connection.
 *
 * @property interfaceField The interface configuration for the tunnel
 * @property peer The peer configuration for the tunnel
 */
data class TunnelConfig(
    @SerializedName("interface")
    val interfaceField: Interface,
    val peer: Peer,
) : Serializable {
    init {
        require(interfaceField.address.isValidIpAddress()) { "Invalid interface address format" }
        require(interfaceField.privateKey.isValidWireGuardKey()) { "Invalid private key format" }
        require(interfaceField.listenPort in 1..65535) { "Invalid listen port" }
        require(peer.publicKey.isValidWireGuardKey()) { "Invalid public key format" }
        require(peer.allowedIps.all { it.isValidIpAddress() }) { "Invalid allowed IPs format" }
        require(peer.endpoint.isValidEndpoint()) { "Invalid endpoint format" }
    }

    companion object {
        private val IP_PATTERN = Pattern.compile(
            "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
        )
        private val WG_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9+/]{43}=$")
        private val ENDPOINT_PATTERN = Pattern.compile(
            "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+$"
        )

        private fun String.isValidIpAddress(): Boolean = IP_PATTERN.matcher(this).matches()
        private fun String.isValidWireGuardKey(): Boolean = WG_KEY_PATTERN.matcher(this).matches()
        private fun String.isValidEndpoint(): Boolean = ENDPOINT_PATTERN.matcher(this).matches()
    }

    /**
     * Builder class for creating TunnelConfig instances.
     */
    class Builder {
        private var interfaceAddress: String? = null
        private var privateKey: String? = null
        private var listenPort: Long = 0
        private var publicKey: String? = null
        private var allowedIps: List<String> = emptyList()
        private var endpoint: String? = null

        fun setInterfaceAddress(address: String) = apply { this.interfaceAddress = address }
        fun setPrivateKey(key: String) = apply { this.privateKey = key }
        fun setListenPort(port: Long) = apply { this.listenPort = port }
        fun setPublicKey(key: String) = apply { this.publicKey = key }
        fun setAllowedIps(ips: List<String>) = apply { this.allowedIps = ips }
        fun setEndpoint(endpoint: String) = apply { this.endpoint = endpoint }

        fun build(): TunnelConfig {
            requireNotNull(interfaceAddress) { "Interface address is required" }
            requireNotNull(privateKey) { "Private key is required" }
            requireNotNull(publicKey) { "Public key is required" }
            requireNotNull(endpoint) { "Endpoint is required" }
            require(allowedIps.isNotEmpty()) { "At least one allowed IP is required" }

            return TunnelConfig(
                interfaceField = Interface(
                    address = interfaceAddress!!,
                    privateKey = privateKey!!,
                    listenPort = listenPort
                ),
                peer = Peer(
                    publicKey = publicKey!!,
                    allowedIps = allowedIps,
                    endpoint = endpoint!!
                )
            )
        }
    }
}

/**
 * Interface configuration for WireGuard tunnel.
 *
 * @property address The IP address for the interface
 * @property privateKey The private key for the interface
 * @property listenPort The port to listen on
 */
data class Interface(
    @SerializedName("Address")
    val address: String,
    @SerializedName("PrivateKey")
    val privateKey: String,
    @SerializedName("ListenPort")
    val listenPort: Long,
) : Serializable

/**
 * Peer configuration for WireGuard tunnel.
 *
 * @property publicKey The public key of the peer
 * @property allowedIps List of allowed IP addresses
 * @property endpoint The endpoint address of the peer
 */
data class Peer(
    @SerializedName("PublicKey")
    val publicKey: String,
    @SerializedName("AllowedIPs")
    val allowedIps: List<String>,
    @SerializedName("Endpoint")
    val endpoint: String,
) : Serializable