# Debug APK

`parking-detection-debug.apk` is generated locally by `./gradlew :android:app:assembleDebug` using `tools/make_debug_apk.py` when Android SDK/AGP downloads are unavailable.

Install the generated APK with:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.zishan.parkingdetection 1
```

A ready-to-download ZIP can be created at `artifacts/parking-detection-debug.zip` with:

```bash
mkdir -p artifacts
cp android/app/build/outputs/apk/debug/app-debug.apk artifacts/parking-detection-debug.apk
( cd artifacts && sha256sum parking-detection-debug.apk > SHA256SUMS.txt && zip -9 parking-detection-debug.zip parking-detection-debug.apk README.txt SHA256SUMS.txt )
```

Note: this artifact path is for restricted environments where Android SDK/AGP downloads are blocked. The full Android source app remains under `android/app` for standard Android builds.
