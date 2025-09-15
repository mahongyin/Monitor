import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
//    id("maven-publish")
//    id("signing")
}

android {
    namespace = "github.leavesczy.monitor"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        consumerProguardFiles.add(File("consumer-rules.pro"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    compileOnly(libs.squareup.okHttp)
}
val VERSION_NAME = libs.versions.monitor.publishing.get()
if (VERSION_NAME.contains("-LOCAL")) {
    //task-> publishMavenPublicationToMavenRepository
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.mahongyin.Monitor"
                artifactId = "monitor-no-op"
                version = VERSION_NAME
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
        // 没有这个，出的包会group空
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
            artifactId = "monitor-no-op",
            version = VERSION_NAME
        )
        pom {
            name = "Monitor"
            description = "An Http inspector for OkHttp & Retrofit"
            inceptionYear = "2025"
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