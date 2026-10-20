plugins {
    kotlin("multiplatform")
}

kotlin {
    // The JVM target stands in for Android: same Kotlin/JVM code, no Android SDK needed to run the demo.
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
