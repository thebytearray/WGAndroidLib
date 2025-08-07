# WireGuard Android Library

A simple Android library for integrating WireGuard VPN functionality into your applications. Provides clean API for managing VPN connections, handling configurations, and monitoring tunnel states.


## Features

- Lightweight and fast with minimal overhead
- Comprehensive API for VPN connection management
- Robust state management with proper error handling
- Real-time traffic statistics monitoring
- Built-in notification system for VPN status
- Easy permission management for VPN and notifications
- Clean configuration using builder pattern
- Comprehensive input validation
- Proper error handling and logging
- Examples provided in both Kotlin and Java

## Installation

### Option 1: Using JitPack Dependency

Add the repository and dependency to your project:

#### Gradle Groovy
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.thebytearray:WGAndroidLib:1.1.0'
}
```

#### Gradle Kotlin DSL
```kotlin
repositories {
   ...
   maven("https://jitpack.io")
}

dependencies {
   implementation("com.github.thebytearray:WGAndroidLib:1.1.0")
}
```

### Option 2: Importing the WireGuard Module

For advanced users who want more control, you can directly include the WireGuard module in your project.

1. Clone the repository:
   ```bash
   git clone https://github.com/thebytearray/WGAndroidLib.git
   ```

2. In Android Studio:
   - Go to File > New > Import Module
   - Select the `wireguard` module folder from the cloned repository
   - Click Finish to add the module to your project

3. Add the module as a dependency:

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

## Setup

### 1. Configure the Application Class

Create an Application class to configure notification channel for VPN usage:

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

### 2. Add Required Permissions and Services

Add these permissions and services to your AndroidManifest.xml:

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Services -->
<service
    android:name="org.thebytearray.wireguard.service.TunnelService"
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

### 3. Initialize the Library

#### Kotlin Example
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set notification icon
        ServiceManager.setNotificationIcon(R.drawable.ic_vpn)
        
        // Check and request permissions
        if (!ServiceManager.hasVpnPermission(this)) {
            ServiceManager.requestVpnPermission(this) { isGranted ->
                if (isGranted) {
                    // VPN permission granted
                }
            }
        }
        
        if (!ServiceManager.hasNotificationPermission(this)) {
            ServiceManager.requestNotificationPermission(this) { isGranted ->
                if (isGranted) {
                    // Notification permission granted
                }
            }
        }
    }
    
    private fun startVpn() {
        // Create VPN configuration using builder pattern
        val config = TunnelConfig.Builder()
            .setInterfaceAddress("10.0.0.2/24")
            .setPrivateKey("YOUR_PRIVATE_KEY")
            .setListenPort(51820)
            .setPublicKey("PEER_PUBLIC_KEY")
            .setAllowedIps(listOf("0.0.0.0/0"))
            .setEndpoint("PEER_ENDPOINT:51820")
            .build()
            
        // Start VPN with configuration
        ServiceManager.startVpnTunnel(this, config, null)
    }
    
    private fun stopVpn() {
        ServiceManager.stopVpnTunnel(this)
    }
}
```

#### Java Example
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set notification icon
        ServiceManager.INSTANCE.setNotificationIcon(R.drawable.ic_vpn);
        
        // Check and request permissions
        if (!ServiceManager.INSTANCE.hasVpnPermission(this)) {
            ServiceManager.INSTANCE.requestVpnPermission(this, isGranted -> {
                if (isGranted) {
                    // VPN permission granted
                }
            });
        }
        
        if (!ServiceManager.INSTANCE.hasNotificationPermission(this)) {
            ServiceManager.INSTANCE.requestNotificationPermission(this, isGranted -> {
                if (isGranted) {
                    // Notification permission granted
                }
            });
        }
    }
    
    private void startVpn() {
        // Create VPN configuration using builder pattern
        TunnelConfig config = new TunnelConfig.Builder()
            .setInterfaceAddress("10.0.0.2/24")
            .setPrivateKey("YOUR_PRIVATE_KEY")
            .setListenPort(51820)
            .setPublicKey("PEER_PUBLIC_KEY")
            .setAllowedIps(Collections.singletonList("0.0.0.0/0"))
            .setEndpoint("PEER_ENDPOINT:51820")
            .build();
            
        // Start VPN with configuration
        ServiceManager.INSTANCE.startVpnTunnel(this, config, null);
    }
    
    private void stopVpn() {
        ServiceManager.INSTANCE.stopVpnTunnel(this);
    }
}
```

## Error Handling

The library provides comprehensive error handling and logging. All major operations are wrapped in try-catch blocks and provide detailed error messages. You can monitor the logs using these tags:

- `ServiceManager`: For service management related logs
- `TunnelService`: For VPN tunnel related logs

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

- **Tamim Hossain**
  - Email: tamimh.dev@gmail.com
  - GitHub: @thebytearray
