// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha15")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    }
}

plugins {
    id("com.diffplug.spotless") version "5.12.4"
    id("com.github.ben-manes.versions") version "0.38.0"
}

val ktLintVersion = "0.41.0"

spotless {
    kotlin {
        ktlint(ktLintVersion)
        target("**/*.kt")
    }
    kotlinGradle {
        ktlint(ktLintVersion)
        target("**/*.gradle.kts")
    }
}
