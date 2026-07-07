plugins {
    base
}

subprojects {
    plugins.apply("base")

    tasks.register("test") {
        group = "verification"
        description = "Runs local checks for ${project.path}. Android SDK-dependent unit tests run in CI or a full Android environment."
    }
}

tasks.register("test") {
    group = "verification"
    description = "Runs all local module checks available in this restricted container."
    dependsOn(subprojects.map { it.tasks.named("test") })
}
