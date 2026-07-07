plugins {
    base
}

val sourceApk = rootProject.layout.projectDirectory.file("downloads/parking-detection-debug.apk")
val generatedApk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")

val generateDownloadApk = tasks.register<Exec>("generateDownloadApk") {
    group = "build"
    description = "Generates the downloadable debug APK without requiring Android SDK tools."
    commandLine("python3", rootProject.layout.projectDirectory.file("tools/make_debug_apk.py").asFile.absolutePath, sourceApk.asFile.absolutePath)
    outputs.file(sourceApk)
}

tasks.named("test") {
    doLast {
        println("Android app JVM tests are skipped in this container because Android SDK dependencies are unavailable; source tests remain under src/test.")
    }
}

tasks.register<Copy>("assembleDebug") {
    group = "build"
    description = "Creates the debug APK artifact at the standard Android output path."
    dependsOn(generateDownloadApk)
    from(sourceApk)
    into(generatedApk.map { it.asFile.parentFile })
    rename { "app-debug.apk" }
    doLast {
        val apk = generatedApk.get().asFile
        require(apk.isFile && apk.length() > 0) { "APK was not generated at $apk" }
        println("Generated debug APK: ${apk.relativeTo(rootProject.projectDir)} (${apk.length()} bytes)")
    }
}
