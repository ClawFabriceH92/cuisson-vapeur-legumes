// Intentionally declares no plugins here (not even `apply false`): a plugin
// referenced in the root plugins{} block is resolved when the root project is
// configured, which happens unconditionally on every invocation — including
// `gradle :domain:test`. Declaring the Android/Hilt/KSP plugins here would
// force resolving the Android Gradle Plugin even for a domain-only test run,
// defeating the point of `org.gradle.configureondemand=true` (see
// gradle.properties and the root README's "Limitations connues"). Each
// module instead declares its own plugins (with versions) in its own
// build.gradle.kts.
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
