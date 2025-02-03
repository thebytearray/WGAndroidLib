
# WireGuard Android Library
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Downloads](https://img.shields.io/github/downloads/CodeWithTamim/WGAndroidLib/total?style=for-the-badge&logo=download)

Simplify the integration of **WireGuard VPN** in your Android applications with this library. This library provides a clean API to manage VPN connections, handle configurations, and monitor tunnel states.

---

## Features

- **Lightweight & Fast**: Minimal overhead with seamless integration.
- **Comprehensive API**: Start, stop, and monitor VPN connections effortlessly.
- **State Broadcasts**: Easily observe and react to connection state changes.
- **Cross-Language Support**: Examples provided in both Kotlin and Java.
- **Customizable**: Option to directly include the library source for deeper customization.

---

## Installation

### Option 1: Using JitPack Dependency

This library is available via [JitPack](https://jitpack.io). Add the repository and dependency to your project:

#### Gradle Groovy
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.CodeWithTamim:WGAndroidLib:1.0.1'
}
```

#### Gradle Kotlin DSL
```kotlin
repositories {
   ...
   maven("https://jitpack.io")
}

dependencies {
   implementation("com.github.CodeWithTamim:WGAndroidLib:1.0.1")
}
```

### Option 2: Importing the WireGuard Module

For advanced users who want more control or customization, you can directly include the WireGuard module in your project using Android Studio.

1. Clone the repository:
   ```bash
   git clone https://github.com/CodeWithTamim/WGAndroidLib.git
   ```

2. In Android Studio, follow these steps:
   - Go to **File** > **New** > **Import Module**.
   - Select the `wireguard` module folder from the cloned repository.
   - Click **Finish** to add the module to your project.

3. Add the module as a dependency in your app's `build.gradle` (Groovy) or `build.gradle.kts` (Kotlin DSL):

#### Gradle Groovy
```gradle
dependencies {
    implementation project(':wireguard')
}
```

#### Gradle Kotlin DSL
```kotlin
dependencies {
    implementation(project(":wireguard"))
}
```

---

## Setup

### 1. Configure the Application Class

Create an `Application` class to configure a notification channel for VPN usage:

#### Kotlin Example
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

#### Java Example
```java
public class TunnelApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }
    }
}
```

### 2. Add Required Permissions

Add these permissions to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 3. Register Services

Declare the required services in `AndroidManifest.xml`:

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

---

## Usage

### Initialize the Service Manager

#### Kotlin Example
```kotlin
ServiceManager.init(this, R.drawable.notification_icon)
```

#### Java Example
```java
ServiceManager.init(this, R.drawable.notification_icon);
```

#### Prepare for VPN Connection

**Kotlin**
```kotlin
if (!ServiceManager.isPreparedForConnection(context)) {
    ServiceManager.prepareForConnection(activity)
}
```

**Java**
```java
if (!ServiceManager.isPreparedForConnection(context)) {
    ServiceManager.prepareForConnection(activity);
}
```

#### Start the VPN

**Kotlin**
```kotlin
val config = TunnelConfig(
    Interface("10.0.0.1/24", "privateKey", 51820),
    Peer("publicKey", listOf("0.0.0.0/0"), "endpoint:51820")
)
ServiceManager.startTunnel(context, config, blockedApps = listOf("com.example.app"))
```

**Java**
```java
TunnelConfig config = new TunnelConfig(
    new Interface("10.0.0.1/24", "privateKey", 51820),
    new Peer("publicKey", Arrays.asList("0.0.0.0/0"), "endpoint:51820")
);
ServiceManager.startTunnel(context, config, Arrays.asList("com.example.app"));
```

#### Stop the VPN

**Kotlin**
```kotlin
ServiceManager.stopTunnel(context)
```

**Java**
```java
ServiceManager.stopTunnel(context);
```

---

## Listen for Tunnel State Changes

Register a broadcast receiver to listen for state changes:

#### Kotlin Example
```kotlin
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra("TUNNEL_STATE")
        // Handle state change: CONNECTED, DISCONNECTED, CONNECTING
    }
}
context.registerReceiver(receiver, IntentFilter("TUNNEL_STATE_ACTION"))
```

#### Java Example
```java
BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra("TUNNEL_STATE");
        // Handle state change: CONNECTED, DISCONNECTED, CONNECTING
    }
};
context.registerReceiver(receiver, new IntentFilter("TUNNEL_STATE_ACTION"));
```

---

## Tunnel Configurations

Define configurations for the VPN connection using the `TunnelConfig` model.

**Kotlin Example**
```kotlin
val config = TunnelConfig(
    Interface("10.0.0.1/24", "privateKey", 51820),
    Peer("publicKey", listOf("0.0.0.0/0"), "endpoint:51820")
)
```

**Java Example**
```java
TunnelConfig config = new TunnelConfig(
    new Interface("10.0.0.1/24", "privateKey", 51820),
    new Peer("publicKey", Arrays.asList("0.0.0.0/0"), "endpoint:51820")
);
```

---

## Tunnel States

The library supports these tunnel states:
- **CONNECTED**: VPN is active and connected.
- **DISCONNECTED**: VPN is stopped.
- **CONNECTING**: VPN is in the process of connecting.

States are broadcasted by the service and can be used to update your UI.

---

## Acknowledgments

This library is based on the [WireGuard Android](https://github.com/WireGuard/wireguard-android) project. Special thanks to the WireGuard team for their incredible work.

---

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.

---

Developed by [Tamim Hossain](mailto:tamimh.dev@gmail.com).
