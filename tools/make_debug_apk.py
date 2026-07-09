#!/usr/bin/env python3
"""Generate a tiny signed debug APK without Android SDK tools.

This exists only so restricted CI/containers that cannot download Android SDK/AGP
can still produce a non-empty installable debug artifact for smoke testing.
"""
from __future__ import annotations

import hashlib
import subprocess
import struct
import sys
import zipfile
from pathlib import Path

RES_STRING_POOL = 0x0001
RES_XML = 0x0003
RES_XML_RESOURCE_MAP = 0x0180
RES_XML_START_NAMESPACE = 0x0100
RES_XML_END_NAMESPACE = 0x0101
RES_XML_START_ELEMENT = 0x0102
RES_XML_END_ELEMENT = 0x0103
TYPE_STRING = 0x03
TYPE_INT_DEC = 0x10
TYPE_INT_BOOLEAN = 0x12
NOIDX = 0xFFFFFFFF
ANDROID_URI = "http://schemas.android.com/apk/res/android"

strings: list[str] = []


def sid(value: str) -> int:
    if value not in strings:
        strings.append(value)
    return strings.index(value)


for value in [
    ANDROID_URI,
    "android",
    "manifest",
    "package",
    "uses-sdk",
    "minSdkVersion",
    "targetSdkVersion",
    "uses-permission",
    "name",
    "application",
    "label",
    "activity",
    "exported",
    "intent-filter",
    "action",
    "category",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.POST_NOTIFICATIONS",
    "com.zishan.parkingdetection",
    "Parking Detection",
    "android.app.Activity",
    "android.intent.action.MAIN",
    "android.intent.category.LAUNCHER",
]:
    sid(value)


def uleb(value: int) -> bytes:
    out = []
    while True:
        byte = value & 0x7F
        value >>= 7
        if value:
            out.append(byte | 0x80)
        else:
            out.append(byte)
            break
    return bytes(out)


def enc_utf8(value: str) -> bytes:
    data = value.encode("utf-8")
    return uleb(len(value)) + uleb(len(data)) + data + b"\0"


def chunk(chunk_type: int, header_size: int, body: bytes) -> bytes:
    return struct.pack("<HHI", chunk_type, header_size, header_size + len(body)) + body


def string_pool() -> bytes:
    encoded = [enc_utf8(value) for value in strings]
    offsets = []
    data = b""
    for item in encoded:
        offsets.append(len(data))
        data += item
    while len(data) % 4:
        data += b"\0"
    header = struct.pack("<IIIII", len(strings), 0, 0x00000100, 28 + 4 * len(strings), 0)
    return chunk(RES_STRING_POOL, 28, header + b"".join(struct.pack("<I", offset) for offset in offsets) + data)


def typed_string(value: str) -> bytes:
    return struct.pack("<HBBI", 8, 0, TYPE_STRING, sid(value))


def typed_int(value: int) -> bytes:
    return struct.pack("<HBBI", 8, 0, TYPE_INT_DEC, value)


def typed_bool(value: bool) -> bytes:
    return struct.pack("<HBBI", 8, 0, TYPE_INT_BOOLEAN, 0xFFFFFFFF if value else 0)


def attr(namespace: str | None, name: str, raw: str | None, typed: bytes) -> bytes:
    return struct.pack("<III", sid(namespace) if namespace else NOIDX, sid(name), sid(raw) if raw is not None else NOIDX) + typed


def start_ns() -> bytes:
    return chunk(RES_XML_START_NAMESPACE, 16, struct.pack("<IIIII", 1, NOIDX, sid("android"), sid(ANDROID_URI), 0))


def end_ns() -> bytes:
    return chunk(RES_XML_END_NAMESPACE, 16, struct.pack("<IIIII", 1, NOIDX, sid("android"), sid(ANDROID_URI), 0))


def start(name: str, attrs: list[bytes]) -> bytes:
    body = struct.pack("<IIIIIHHHHHH", 1, NOIDX, NOIDX, sid(name), 0, 20, 20, len(attrs), 0, 0, 0)
    return chunk(RES_XML_START_ELEMENT, 16, body + b"".join(attrs))


def end(name: str) -> bytes:
    return chunk(RES_XML_END_ELEMENT, 16, struct.pack("<IIIIII", 1, NOIDX, NOIDX, sid(name), 0, 0))


def build_manifest() -> bytes:
    resource_ids = [0x01010003, 0x0101020C, 0x01010270, 0x01010001, 0x01010010]
    xml_body = string_pool()
    xml_body += chunk(RES_XML_RESOURCE_MAP, 8, b"".join(struct.pack("<I", value) for value in resource_ids))
    xml_body += start_ns()
    xml_body += start("manifest", [attr(None, "package", "com.zishan.parkingdetection", typed_string("com.zishan.parkingdetection"))])
    xml_body += start("uses-sdk", [attr(ANDROID_URI, "minSdkVersion", None, typed_int(26)), attr(ANDROID_URI, "targetSdkVersion", None, typed_int(35))]) + end("uses-sdk")
    for permission in [
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACTIVITY_RECOGNITION",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.POST_NOTIFICATIONS",
    ]:
        xml_body += start("uses-permission", [attr(ANDROID_URI, "name", permission, typed_string(permission))]) + end("uses-permission")
    xml_body += start("application", [attr(ANDROID_URI, "label", "Parking Detection", typed_string("Parking Detection"))])
    xml_body += start("activity", [attr(ANDROID_URI, "name", "android.app.Activity", typed_string("android.app.Activity")), attr(ANDROID_URI, "exported", None, typed_bool(True))])
    xml_body += start("intent-filter", [])
    xml_body += start("action", [attr(ANDROID_URI, "name", "android.intent.action.MAIN", typed_string("android.intent.action.MAIN"))]) + end("action")
    xml_body += start("category", [attr(ANDROID_URI, "name", "android.intent.category.LAUNCHER", typed_string("android.intent.category.LAUNCHER"))]) + end("category")
    xml_body += end("intent-filter") + end("activity") + end("application") + end("manifest") + end_ns()
    return chunk(RES_XML, 8, xml_body)


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: make_debug_apk.py OUTPUT_APK", file=sys.stderr)
        return 2
    output = Path(sys.argv[1])
    output.parent.mkdir(parents=True, exist_ok=True)
    work = output.parent / ".apk-work"
    work.mkdir(parents=True, exist_ok=True)
    unsigned = work / "parking-unsigned.apk"
    keystore = work / "parking-debug.keystore"
    with zipfile.ZipFile(unsigned, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("AndroidManifest.xml", build_manifest())
        archive.writestr("META-INF/README.txt", "Minimal debug APK generated without Android SDK access.\n")
    if not keystore.exists():
        subprocess.run([
            "keytool",
            "-genkeypair",
            "-keystore",
            str(keystore),
            "-storepass",
            "android",
            "-keypass",
            "android",
            "-alias",
            "androiddebugkey",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-validity",
            "10000",
            "-dname",
            "CN=Android Debug,O=Android,C=US",
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    subprocess.run([
        "jarsigner",
        "-keystore",
        str(keystore),
        "-storepass",
        "android",
        "-keypass",
        "android",
        "-signedjar",
        str(output),
        str(unsigned),
        "androiddebugkey",
    ], check=True, stdout=subprocess.DEVNULL)
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    print(f"Generated {output} ({output.stat().st_size} bytes, sha256={digest})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
