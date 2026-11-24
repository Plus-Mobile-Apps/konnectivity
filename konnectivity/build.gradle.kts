import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
}

group = "com.plusmobileapps"
version = libs.versions.konnectivity.get()

kotlin {
    android {
        namespace = "com.plusmobileapps.konnectivity"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
    iosArm64()
    iosSimulatorArm64()
    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
    js {
        browser()
        nodejs()
        binaries.library()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.library()
    }

    cocoapods {
        summary = "A kotlin multiplatform network connectivity checker"
        homepage = "https://github.com/plusmobileapps/konnectivity"
        version = libs.versions.konnectivity.get()
        ios.deploymentTarget = "15.0"
        framework {
            baseName = "konnectivity"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.startup)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    // Only sign when a key is configured so `publishToMavenLocal` works without secrets.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    coordinates("com.plusmobileapps", "konnectivity", libs.versions.konnectivity.get())

    pom {
        name.set("Konnectivity")
        description.set("A kotlin multiplatform library for checking the current network connectivity status")
        url.set("https://github.com/plusmobileapps/konnectivity")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("plusmobileapps")
                name.set("Andrew Steinmetz")
                email.set("andrew@plusmobileapps.com")
            }
        }
        scm {
            url.set("https://github.com/plusmobileapps/konnectivity")
        }
    }
}
