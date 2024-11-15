
# WireGuard Android Library

[![Release](https://jitpack.io/v/username/wireguard-android.svg)](https://jitpack.io/#username/wireguard-android)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This library simplifies the integration of WireGuard VPN in Android applications. With this library, you can easily start, stop, and manage VPN connections programmatically.

## Features
- Simple integration for WireGuard VPN in Android apps
- Manage VPN states and configurations
- Support for broadcasting tunnel states
- Lightweight and easy to use

## Getting Started

### 1. Add Dependency

Add JitPack to your project-level `build.gradle` file:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the library dependency to your module-level `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.CodeWithTamim:wireguard-android:Tag'
}
```

### 2. Application Setup

Create an `Application` class and configure a notification channel:

```kotlin
class TunnelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(this)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}
```

### 3. Add Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 4. Declare Services

Add these service declarations to your `AndroidManifest.xml`:

```xml
<service
    android:name="com.nasahacker.wireguard.service.TunnelService"
    android:exported="true"
    android:foregroundServiceType="specialUse"
    android:permission="android.permission.FOREGROUND_SERVICE" />

<service
    android:name="com.wireguard.android.backend.GoBackend$VpnService"
    android:exported="true"
    android:permission="android.permission.BIND_VPN_SERVICE">
    <intent-filter>
        <action android:name="android.net.VpnService" />
    </intent-filter>
</service>
```

### 5. Using the Library

#### Initialize the Service Manager

In your activity, initialize the `ServiceManager`:

```kotlin
ServiceManager.init(this, R.drawable.notification_icon)
```

#### Start and Stop VPN

Prepare for connection:

```kotlin
if (!ServiceManager.isPreparedForConnection(context)) {
    ServiceManager.prepareForConnection(activity)
}
```

Start the VPN:

```kotlin
val config = TunnelConfig(
    Interface("10.0.0.1/24", "privateKey", 51820),
    Peer("publicKey", listOf("0.0.0.0/0"), "endpoint:51820")
)
ServiceManager.startTunnel(context, config, blockedApps = listOf("com.example.app"))
```

Stop the VPN:

```kotlin
ServiceManager.stopTunnel(context)
```

#### Receive Tunnel State Broadcasts

Register a broadcast receiver to listen for tunnel state changes:

```kotlin
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra("TUNNEL_STATE")
        // Handle state change: CONNECTED, DISCONNECTED, CONNECTING
    }
}
context.registerReceiver(receiver, IntentFilter("TUNNEL_STATE_ACTION"))
```

### 6. Tunnel Configuration

Define the `TunnelConfig` model:

```kotlin
val config = TunnelConfig(
    Interface("10.0.0.1/24", "privateKey", 51820),
    Peer("publicKey", listOf("0.0.0.0/0"), "endpoint:51820")
)
```

### 7. Tunnel States

The library supports the following tunnel states:
- `CONNECTED`
- `DISCONNECTED`
- `CONNECTING`

These states are broadcasted by the service and can be used to update the UI.

## License

This library is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.

---

Developed by [Tamim Hossain](mailto:tamimh.dev@gmail.com).
