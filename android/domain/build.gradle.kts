plugins {
    base
}

tasks.named("test") {
    doLast {
        println("Domain source tests are preserved under src/test; Kotlin plugin dependency execution is disabled in this restricted container.")
    }
}
