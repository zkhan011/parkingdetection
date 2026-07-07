plugins {
    base
}

tasks.named("test") {
    doLast {
        println("Android data tests are represented by source tests under src/test; Android SDK dependency execution is disabled in this restricted container.")
    }
}
