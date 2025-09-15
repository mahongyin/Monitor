buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("./localmaven/") }
        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
    dependencies {
//        classpath("com.vanniktech:gradle-maven-publish-plugin:0.34.0")
        classpath("io.github.mahongyin.Monitor:monitor-plugin:2.0.0-SNAPSHOT")
    }
}

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.compose).apply(false)
    alias(libs.plugins.androidx.room).apply(false)
    alias(libs.plugins.google.ksp).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
//    alias(libs.plugins.monitor.plugin).apply(false)
}