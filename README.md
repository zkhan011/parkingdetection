# Parking Detection

Native Android and iOS workspace for automatic parked-vehicle detection. The Android project is a real Gradle/Android project with `:android:app`, `:android:data`, and `:android:domain`; the iOS core is a Swift Package under `ios/ParkingDetection`.

## Architecture

```text
android/app      Android application module, launcher activity, Compose UI, permissions, services
android/data     Android library module for Room, DataStore, location, Bluetooth, repositories
android/domain   Pure Kotlin/JVM detection models, scoring, state machine, buffers, tests
ios/ParkingDetection   Swift Package containing iOS-side detection core and tests
docs             Architecture, permissions, privacy, scoring, limitations, implementation plans
```

Gradle hierarchy:

```text
Root project 'ParkingDetection'
+--- Project ':android'
|    +--- Project ':android:app'
|    +--- Project ':android:data'
|    \--- Project ':android:domain'
```

## Required versions

- Java 21.
- Gradle 8.10.2. The text-only `./gradlew` bootstrapper downloads Gradle 8.10.2 into `.gradle/bootstrap` when `GRADLE_HOME` is not set, and falls back to `gradle` on `PATH` if the download is blocked, because the PR system rejects binary files such as `gradle-wrapper.jar`.
- Android Gradle Plugin 8.8.2.
- Kotlin 2.1.10.
- Android SDK platform `android-35` and Build Tools `35.0.0`.
- Android Studio Koala/Meerkat-era or newer stable release capable of AGP 8.8.x.

## Linux setup

```bash
git clone https://github.com/zkhan011/parkingdetection.git
cd parkingdetection
chmod +x gradlew devandroid.sh
export JAVA_HOME=/path/to/jdk21
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
./gradlew --version
./gradlew projects
./gradlew clean test
./gradlew :android:app:assembleDebug
```

If SDK packages are missing:

```bash
sdkmanager \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0"
yes | sdkmanager --licenses
```

Or run the helper:

```bash
./devandroid.sh https://github.com/zkhan011/parkingdetection.git /tmp/parkingdetection-build
```

## Build and test commands

```bash
./gradlew --version
./gradlew projects
./gradlew clean
./gradlew test
./gradlew :android:domain:test
./gradlew :android:app:testDebugUnitTest
./gradlew :android:app:assembleDebug
```

The debug APK is generated at:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

Install and launch:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.zishan.parkingdetection 1
```



## Local parking map and speed detection

The Home screen contains a local, offline schematic map that shows the current parked vehicle in blue, its accepted 35 m accuracy radius, and recent parking-history markers in grey. It does not download Google Maps tiles or require an API key.

While the app is open and precise foreground location is granted, automatic detection samples balanced-power location updates. After the phone has been travelling at 15 km/h or more, the app treats speed at or below 2 km/h for 90 seconds with a 35 m or better fix as a parking candidate. The baseline confidence is 60 and existing cooldown settings prevent duplicate saves for the configured interval. This is a foreground baseline; background detection remains subject to Android permissions and system restrictions.

## Manual parking accuracy

Manual parking only saves a fresh **Precise location** reading. The app requests foreground precise-location permission when the button is tapped, waits up to 20 seconds for Fused Location Provider accuracy, and refuses to save readings worse than 35 metres. It never substitutes a default city or stale coordinate. If saving is refused, enable precise location, turn on GPS, move outdoors, and retry.

## Permissions

The Android app declares foreground/background location, activity recognition, Bluetooth, notification, and foreground-service permissions. Permissions must be requested incrementally in the UI; manual parking should continue to work with optional background permissions denied.

## Background detection limitations

Android background execution is restricted by OS version, battery mode, and permission state. The app should use Activity Recognition transitions, Bluetooth disconnection, balanced location updates, WorkManager, and a foreground service only when required. Do not expect Google Maps-level parked-car accuracy without real-world device testing and tuned signal fusion.

## Troubleshooting

- `./gradlew --version` fails: ensure `curl` or `wget` and `unzip` are installed, allow access to `services.gradle.org`, set `GRADLE_HOME` to an existing Gradle 8.10.2 installation, or install Gradle on `PATH` for fallback execution. Binary `gradle-wrapper.jar` is intentionally not committed because this PR system rejects binary files.
- Plugin resolution fails: verify access to Google Maven, Maven Central, and Gradle Plugin Portal.
- Android SDK not found: set `ANDROID_HOME` or `ANDROID_SDK_ROOT`, then install `platforms;android-35` and `build-tools;35.0.0`.
- Wrong Java version: use Java 21 and make sure `java -version` reports 21.
- Device install fails: run `adb devices`, enable USB debugging, and accept the device authorization prompt.

## iOS tests

```bash
cd ios/ParkingDetection
swift test
```
