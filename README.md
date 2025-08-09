# WGAndroidLib - Simple Android Wireguard Library

#### This library is a wrapper around the main wireguard android project intended to simplify the implementation of wireguard in android apps without handling complex functions.

#### It's production ready and provides simple and easy abstraction layer and functions to work with wireguard.

#### Features :

* Provides Dev Friendly Apis to start/stop/monitor connection.
* State management through broadcast receiver.
* Built in notification system.
* Validation logic for wireguard configurations.
* Easy permission handling for VPNService and Notification.

#### Installation and Api Documentation

#### Installation Documentation

#### Option 1 : Using jitpack builds

#### Add the jitpack repository and dependency to your app settings.gradle

#### Groovy

```groovy
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

#### Kotlin DSL

```kotlin
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.thebytearray:WGAndroidLib:1.1.0")
}
```

#### And then sync the project and follow the api usage guide.

#### Option 2 : Importing and using as a sub-module

#### This step is for advanced devs who want to use the library as sub module and later maybe want to do some changes to tailor to their needs .

#### 1.Clone the repository.

```
git clone https://github.com/thebytearray/WGAndroidLib.git
```

#### 2.In Android Studio :

#### Go to File > New > Import Module , and then select the

`wireguard` module from the cloned project and then add it to your project.

#### 3.Add the module dependency.

#### Groovy (App Level)

```groovy
dependencies {
    implementation project(':wireguard')
}
```

#### Kotlin DSL (App Level)

```kotlin
dependencies {
    implementation(project(":wireguard"))
}
```

#### Api Documentation

#### Setup

#### 1.Configure your Application class (host app) to create a notification channel that will be used for the service of wireguard.

#### Examples

#### Kotlin

```kotlin
class TunnelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        //Imports are not shown here you need to import the CHANNEL_ID and CHANNEL_NAME
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

#### Java
```java
public class TunnelApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Imports are not shown here you need to import the CHANNEL_ID and CHANNEL_NAME
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
#### 2.Configure permissions and service declaration in AndroidManifest.xml
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

#### 3.Configure and initialize the library
#### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set notification icon -> for the service :
        ServiceManager.setNotificationIcon(R.drawable.ic_vpn)
        
        // Check and request permissions if not granted
        if (!ServiceManager.hasVpnPermission(this)) {
            ServiceManager.requestVpnPermission(this) { isGranted ->
                if (isGranted) {
                    // VPN permission granted -> you can start now safely
                }
            }
        }
        
        if (!ServiceManager.hasNotificationPermission(this)) {
            ServiceManager.requestNotificationPermission(this) { isGranted ->
                if (isGranted) {
                    // Notification permission granted ,optional though
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

#### Java
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set notification icon -> for the service :
        ServiceManager.INSTANCE.setNotificationIcon(R.drawable.ic_vpn);
        
        // Check and request permissions if not granted
        if (!ServiceManager.INSTANCE.hasVpnPermission(this)) {
            ServiceManager.INSTANCE.requestVpnPermission(this, isGranted -> {
                if (isGranted) {
                    // VPN permission granted -> you can start now safely
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
#### Error handling
#### The library itself handles some of the most common errors, specifically related to configuration validation. If you want to handle some yourself if you have then it's recommended to follow the option 2 and import the library as a module and use it.

#### License
#### This Project is licensed under the Apache 2.0 - See LICENSE for more details.
#### Acknowledgement
#### [wireguard-android](https://github.com/WireGuard/wireguard-android)
#### WireGuard® is a registered trademark of Jason A. Donenfeld 

