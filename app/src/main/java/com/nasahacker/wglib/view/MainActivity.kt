package com.nasahacker.wglib.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nasahacker.wglib.R
import com.nasahacker.wglib.adapter.AllowedIpsAdapter
import com.nasahacker.wireguard.model.Interface
import com.nasahacker.wireguard.model.Peer
import com.nasahacker.wireguard.model.TunnelConfig
import com.nasahacker.wireguard.service.ServiceManager

class MainActivity : AppCompatActivity() {

    private lateinit var allowedIpsAdapter: AllowedIpsAdapter
    private lateinit var addressInput: TextInputEditText
    private lateinit var privateKeyInput: TextInputEditText
    private lateinit var listenPortInput: TextInputEditText
    private lateinit var publicKeyInput: TextInputEditText
    private lateinit var endpointInput: TextInputEditText
    private lateinit var allowedIpInput: TextInputEditText // Newly added input for allowed IP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ServiceManager.init(this, R.mipmap.ic_launcher)

        // Adjust padding for insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize input fields
        addressInput = findViewById(R.id.interfaceAddress)
        privateKeyInput = findViewById(R.id.interfacePrivateKey)
        listenPortInput = findViewById(R.id.interfaceListenPort)
        publicKeyInput = findViewById(R.id.peerPublicKey)
        endpointInput = findViewById(R.id.peerEndpoint)
        allowedIpInput =
            findViewById(R.id.allowedIpInput) // Correctly reference the allowed IP input

        // Initialize the adapter and RecyclerView
        val allowedIpsRecyclerView: RecyclerView = findViewById(R.id.allowedIpsRecyclerView)
        allowedIpsAdapter = AllowedIpsAdapter(mutableListOf())
        allowedIpsRecyclerView.adapter = allowedIpsAdapter
        allowedIpsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add Allowed IP functionality
        val addAllowedIpButton: MaterialButton = findViewById(R.id.addAllowedIpButton)
        addAllowedIpButton.setOnClickListener {
            val ip = allowedIpInput.text.toString()
            if (ip.isNotEmpty()) {
                allowedIpsAdapter.addAllowedIp(ip)
                allowedIpInput.text?.clear() // Clear input field after adding
            } else {
                Toast.makeText(this, "IP cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        // Start Tunnel Button
        val startTunnelButton: MaterialButton = findViewById(R.id.startTunnelButton)
        startTunnelButton.setOnClickListener {
            val tunnelConfig = buildTunnelConfig()
            if (tunnelConfig != null) {
                if (!ServiceManager.isPreparedForConnection(this)) {
                    ServiceManager.prepareForConnection(this)
                } else {
                    ServiceManager.startTunnel(this, tunnelConfig, null)
                }
            }
        }

        // Stop Tunnel Button
        val stopTunnelButton: MaterialButton = findViewById(R.id.stopTunnelButton)
        stopTunnelButton.setOnClickListener {
            ServiceManager.stopTunnel(this)
        }
    }

    private fun buildTunnelConfig(): TunnelConfig? {
        val address = addressInput.text.toString()
        val privateKey = privateKeyInput.text.toString()
        val listenPort = listenPortInput.text.toString().toLongOrNull()
        val publicKey = publicKeyInput.text.toString()
        val endpoint = endpointInput.text.toString()
        val allowedIps = allowedIpsAdapter.getAllowedIps()

        if (address.isEmpty() || privateKey.isEmpty() || listenPort == null || publicKey.isEmpty() || endpoint.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show()
            return null
        }

        val interfaceConfig = Interface(
            address = address,
            privateKey = privateKey,
            listenPort = listenPort
        )

        val peerConfig = Peer(
            publicKey = publicKey,
            allowedIps = allowedIps,
            endpoint = endpoint
        )

        return TunnelConfig(
            interfaceField = interfaceConfig,
            peer = peerConfig
        )
    }
}
