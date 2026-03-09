import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.maven.publish)
//    id("maven-publish")
//    id("signing")
}

//val signingKeyId = properties["signing.keyId"]?.toString()

android {
    namespace = "github.leavesczy.monitor"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
        consumerProguardFiles.add(File("consumer-rules.pro"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            optIn.addAll(
                setOf(
                    "androidx.room.ExperimentalRoomApi",
                    "androidx.paging.ExperimentalPagingApi",
                    "kotlinx.coroutines.DelicateCoroutinesApi"
                )
            )
        }
    }
    buildFeatures {
        compose = true
    }
    room {
        generateKotlin = true
        schemaDirectory("$projectDir/build/schemas")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.compose)
    implementation(libs.google.gson)
    compileOnly(libs.squareup.okHttp)
    compileOnly("com.tencent.tbs:tbssdk:44286")//44382
}
val VERSION_NAME = libs.versions.monitor.publishing.get()
if (VERSION_NAME.contains("-LOCAL")) {
    //if (signingKeyId == null) {
    publishing {
        publications {
            create<MavenPublication>("release") {
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
        group = "io.github.mahongyin.Monitor"
        version = VERSION_NAME
        repositories {
            maven {
                url = uri("../localmaven")
            }
        }
    }
} else {
    mavenPublishing {
//        publishToMavenCentral()
//        signAllPublications()
        configure(platform = AndroidSingleVariantLibrary())
        coordinates(
            groupId = "io.github.mahongyin.Monitor",
            artifactId = "monitor",
            version = VERSION_NAME
        )
        pom {
            name = "Monitor"
            description = "An Http inspector for OkHttp & Retrofit"
            inceptionYear = "2026"
            url = "https://github.com/mahongyin/Monitor"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "mahongyin"
                    name = "mahongyin"
                    url = "https://github.com/mahongyin"
                }
            }
            scm {
                url = "https://github.com/mahongyin/Monitor"
                connection = "scm:git:git://github.com/mahongyin/Monitor.git"
                developerConnection = "scm:git:ssh://git@github.com/mahongyin/Monitor.git"
            }
        }
    }
}