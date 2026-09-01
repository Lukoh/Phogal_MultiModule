plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.goforer.phogal.core.network"
    compileSdk = 37
    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "apiServer", "\"https://api.unsplash.com/\"")
        buildConfigField("String", "clientId", "\"V9sYHDmwcPc46chEOLA_bhTV3hwsWG0P1ta1vNZjmLs\"")
        buildConfigField("int", "VERSION_CODE", "1")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures { buildConfig = true }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)
    api(libs.retrofit)
    implementation(libs.coil)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.okio)
    implementation(libs.persistent.cookie.jar)
    implementation(libs.logger)
    implementation(libs.timber)
    implementation(libs.androidx.paging.common.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
