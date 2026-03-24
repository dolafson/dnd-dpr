
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()
/*
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
*/

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            // implementation(project(":dprLib"))
            // implementation("io.github.kotlin:library:1.0.0")
            // implementation("com.vikinghelmet.dnd:dprlib:1.0.0")
            implementation(project(":dprlib"))

            implementation(compose.components.resources)

            //implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta03") // Check for the latest compatible version
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2") // Check for the latest compatible version
            //implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")

            // https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.10.3
            //implementation("org.jetbrains.androidx.navigation:navigation-*:2.9.2")
            //implementation("org.jetbrains.androidx.navigation3:navigation3-*:1.0.0-alpha06")
            //implementation("org.jetbrains.androidx.navigationevent:navigationevent-compose:1.0.1")


            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
//            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
//            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")//2.6.1")
        }
        webMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.22.0"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.core)
            //implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0-alpha01")//2.6.1")
            // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.3")

//            https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-swing/1.6.3/kotlinx-coroutines-swing-1.6.3.pom

        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.vikinghelmet.dnd.dprapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.vikinghelmet.dnd.dprapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.vikinghelmet.dnd.dprapp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.vikinghelmet.dnd.dprapp"
            packageVersion = "1.0.0"
        }
    }
}
