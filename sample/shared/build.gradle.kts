plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.plusmobileapps.konnectivity.sample"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.sample.minSdk.get().toInt()
    }
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            export(libs.decompose)
            export(libs.essenty.lifecycle)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":konnectivity"))
            implementation(libs.kotlinx.coroutines.core)
            api(libs.decompose)
            api(libs.essenty.lifecycle)
            implementation(libs.mvikotlin)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.extensions.coroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
