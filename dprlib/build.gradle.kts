
import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    id("maven-publish")
    kotlin("plugin.serialization") version "2.1.20" // Use the same Kotlin version
}

group = "com.vikinghelmet.dnd"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    /*
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // ...
        binaries.executable()
    }
    js {
        browser()
        binaries.executable()
    }
    */
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)

            //implementation("io.github.aakira:napier:2.7.1")
            //api("com.diamondedge:logging:2.1.0")
            //implementation("com.diamondedge:logging:2.1.0")

            //implementation("com.diamondedge:logging-android:2.1.0")
            //implementation("com.diamondedge:logging-jvm:2.1.0")
            //implementation("com.diamondedge:logging-js:2.1.0")

            implementation("io.github.shivathapaa:logger:1.2.0")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation("org.slf4j:slf4j-api:2.0.16")
            implementation("ch.qos.logback:logback-classic:1.5.11")
            implementation("org.junit.jupiter:junit-jupiter:5.7.1")
            implementation("org.junit.jupiter:junit-jupiter-params:5.1.0")
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

tasks.register<Copy>("copySharedResourcesForTest") {
    from(layout.projectDirectory.dir("../shared/resources"))
    into(layout.buildDirectory.dir("processedResources/jvm/test"))
}

tasks.configureEach {
    // Ensure files are copied BEFORE the resource accessors are generated
    if (name.startsWith("jvmTest")) {
        dependsOn("copySharedResourcesForTest")
    }
}

tasks.withType<Test>().configureEach {
    maxHeapSize = "2G"
    useJUnitPlatform()
    systemProperty("RunSlowTests", System.getProperty("RunSlowTests"))
}

mavenPublishing {
    publishToMavenCentral()

    // signAllPublications()

    coordinates(group.toString(), "dprlib", version.toString())

    pom {
        name = "DPR Library"
        description = "DPR library."
        inceptionYear = "2026"
        url = "https://github.com/dolafson/dnd-dpr"
    }
}
