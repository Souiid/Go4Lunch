plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.idrisssouissi.go4lunch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.idrisssouissi.go4lunch"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    
    // FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)


    // FACEBOOK SDK
    implementation(libs.facebook.android.sdk) {
        exclude(group = "com.android.support", module = "customtabs")
    }

    // ADD EXPLICITLY THE BROWSER DEPENDENCY
    implementation("androidx.browser:browser:1.4.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    implementation("com.mikhaellopez:circularimageview:4.3.1")


    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
}

configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.13.0")
        force("androidx.media:media:1.0.0")
    }
}
