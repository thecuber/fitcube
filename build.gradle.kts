// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.googleRelay)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.compose.compiler) apply false
}