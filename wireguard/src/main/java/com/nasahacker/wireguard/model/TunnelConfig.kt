package com.nasahacker.wireguard.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * CodeWithTamim
 *
 * @developer Tamim Hossain
 * @mail tamimh.dev@gmail.com
 */
data class TunnelConfig(
    @SerializedName("interface")
    val interfaceField: Interface,
    val peer: Peer,
) : Serializable

data class Interface(
    @SerializedName("Address")
    val address: String,
    @SerializedName("PrivateKey")
    val privateKey: String,
    @SerializedName("ListenPort")
    val listenPort: Long,
) : Serializable

data class Peer(
    @SerializedName("PublicKey")
    val publicKey: String,
    @SerializedName("AllowedIPs")
    val allowedIps: List<String>,
    @SerializedName("Endpoint")
    val endpoint: String,
) : Serializable