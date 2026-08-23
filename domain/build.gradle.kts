// Pure Kotlin/JVM module — zero Android dependencies on purpose so it can be
// built and unit-tested with a plain JDK + Gradle, without an Android SDK
// (see root README, "Limitations connues"). Contains the vegetable catalog,
// the cooking-plan algorithm (EF-16) and the nutrition-goals algorithm (EF-14).
plugins {
    kotlin("jvm") version "2.0.21"
}

// Repositories are declared once in settings.gradle.kts
// (dependencyResolutionManagement, FAIL_ON_PROJECT_REPOS) — no per-module
// repositories{} block needed or allowed here.

// No explicit jvmToolchain(): this keeps the build using whatever JDK runs
// Gradle itself (JDK 21 in this sandbox) instead of trying to auto-provision
// a different toolchain over the network, which is not reliable here.

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
