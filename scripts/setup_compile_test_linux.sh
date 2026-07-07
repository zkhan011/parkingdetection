#!/usr/bin/env bash
set -euo pipefail

# ParkingDetection Linux setup/build/test helper.
#
# Usage:
#   ./scripts/setup_compile_test_linux.sh [GIT_URL] [WORKDIR]
#
# Examples:
#   ./scripts/setup_compile_test_linux.sh https://github.com/your-org/parkingdetection.git /tmp/parkingdetection-build
#   ./scripts/setup_compile_test_linux.sh . /tmp/parkingdetection-build
#   ./scripts/setup_compile_test_linux.sh
#
# What it does:
#   1. Installs/checks required command-line tools where possible.
#   2. Clones or copies the repository.
#   3. Selects Java 17 when available.
#   4. Runs Android local Gradle checks and assembles the debug APK.
#   5. Runs Swift package tests when Swift is installed.
#   6. Packages the APK into artifacts/parking-detection-debug.zip.
#
# Notes:
#   - This repository intentionally uses a text-only ./gradlew launcher. It requires
#     a Gradle executable on PATH.
#   - The restricted-container APK path is generated without Android SDK/AGP downloads.
#   - For a production Android build, use a normal Android SDK/AGP environment.

GIT_URL="${1:-.}"
WORKDIR="${2:-${PWD}/parkingdetection-linux-build}"
REPO_DIR="${WORKDIR}/parkingdetection"
APP_ID="com.zishan.parkingdetection"
APK_PATH="android/app/build/outputs/apk/debug/app-debug.apk"
ZIP_PATH="artifacts/parking-detection-debug.zip"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
warn() { printf '\n\033[1;33mWARNING: %s\033[0m\n' "$*" >&2; }
fail() { printf '\n\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }
have() { command -v "$1" >/dev/null 2>&1; }

log "Preparing workspace: ${WORKDIR}"
mkdir -p "${WORKDIR}"

if [[ "${GIT_URL}" == "." || -d "${GIT_URL}/.git" ]]; then
  log "Copying local repository from ${GIT_URL}"
  rm -rf "${REPO_DIR}"
  mkdir -p "${REPO_DIR}"
  (cd "${GIT_URL}" && git archive --format=tar HEAD) | (cd "${REPO_DIR}" && tar xf -)
else
  log "Cloning repository: ${GIT_URL}"
  rm -rf "${REPO_DIR}"
  git clone "${GIT_URL}" "${REPO_DIR}"
fi

cd "${REPO_DIR}"
log "Repository ready at ${REPO_DIR}"
git status --short || true

log "Checking required tools"
have git || fail "git is required"
have java || fail "Java is required. Install Java 17, for example Temurin/OpenJDK 17."
have gradle || fail "gradle is required on PATH because this repository uses a text-only ./gradlew launcher."
have python3 || fail "python3 is required to generate the restricted-container debug APK."
have keytool || fail "keytool is required to create a debug signing key."
have jarsigner || fail "jarsigner is required to sign and verify the debug APK."
have zip || fail "zip is required to package the downloadable artifact."
have unzip || fail "unzip is required to inspect the downloadable artifact."

if [[ -d "${HOME}/.local/share/mise/installs/java/17.0.2" ]]; then
  export JAVA_HOME="${HOME}/.local/share/mise/installs/java/17.0.2"
  export PATH="${JAVA_HOME}/bin:${PATH}"
elif [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="${JAVA_HOME}/bin:${PATH}"
else
  warn "JAVA_HOME is not set; using java from PATH. Java 17 is recommended."
fi

log "Tool versions"
java -version
gradle --version | sed -n '1,20p'
python3 --version
if have swift; then swift --version | sed -n '1,5p'; else warn "swift not found; iOS Swift tests will be skipped on this Linux machine."; fi
if have adb; then adb version | sed -n '1,5p'; else warn "adb not found; install/launch verification will be skipped."; fi

log "Running Android local checks and assembling debug APK"
chmod +x ./gradlew
./gradlew clean test :android:app:assembleDebug --stacktrace

log "Validating APK output"
[[ -s "${APK_PATH}" ]] || fail "Expected APK was not generated at ${APK_PATH}"
find . -type f -name "*.apk" -size +0 -print
wc -c "${APK_PATH}"
sha256sum "${APK_PATH}"
jarsigner -verify -certs -verbose "${APK_PATH}" | sed -n '1,90p'

log "Packaging downloadable ZIP"
mkdir -p artifacts
cp "${APK_PATH}" artifacts/parking-detection-debug.apk
cat > artifacts/README.txt <<README
Parking Detection debug APK package

Install on a connected Android device or emulator:
adb install -r parking-detection-debug.apk
adb shell monkey -p ${APP_ID} 1
README
(
  cd artifacts
  sha256sum parking-detection-debug.apk > SHA256SUMS.txt
  zip -9 -FS parking-detection-debug.zip parking-detection-debug.apk README.txt SHA256SUMS.txt
)
[[ -s "${ZIP_PATH}" ]] || fail "Expected ZIP was not generated at ${ZIP_PATH}"
unzip -l "${ZIP_PATH}"
sha256sum "${ZIP_PATH}"

if have swift && [[ -f ios/ParkingDetection/Package.swift ]]; then
  log "Running iOS Swift package tests"
  (cd ios/ParkingDetection && swift test)
fi

if have adb; then
  log "Checking for connected Android devices"
  adb devices
  if adb get-state >/dev/null 2>&1; then
    log "Installing APK on connected device/emulator"
    adb install -r "${APK_PATH}"
    log "Launching ${APP_ID}"
    adb shell monkey -p "${APP_ID}" 1
  else
    warn "No adb device/emulator is connected; skipping install and launch."
  fi
fi

log "Done"
printf '\nAPK: %s/%s\n' "${REPO_DIR}" "${APK_PATH}"
printf 'ZIP: %s/%s\n' "${REPO_DIR}" "${ZIP_PATH}"
printf '\nInstall manually with:\n  adb install -r %s/%s\n  adb shell monkey -p %s 1\n' "${REPO_DIR}" "${APK_PATH}" "${APP_ID}"
