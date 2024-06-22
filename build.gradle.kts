// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.relay") version "0.3.12"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.dagger.hilt.android") version "2.51" apply false
}