buildscript {
    dependencies {
        // AGP 9 uses built-in Kotlin; this upgrades its runtime compiler to the
        // current Kotlin version used by the Compose compiler plugin.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}
