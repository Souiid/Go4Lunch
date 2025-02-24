
plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.idrisssouissi.go4lunch"
    compileSdk = 34

    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.idrisssouissi.go4lunch"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["GOOGLE_GEO_API_KEY"] = project.findProperty("GOOGLE_GEO_API_KEY") as String? ?: ""

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GOOGLE_API_KEY", "\"${project.findProperty("GOOGLE_API_KEY") ?: ""}\"" )
            buildConfigField("String", "GOOGLE_GEO_API_KEY", "\"${project.findProperty("GOOGLE_GEO_API_KEY") ?: ""}\"" )
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "GOOGLE_API_KEY", "\"${project.findProperty("GOOGLE_API_KEY") ?: ""}\"" )
            buildConfigField("String", "GOOGLE_GEO_API_KEY", "\"${project.findProperty("GOOGLE_GEO_API_KEY") ?: ""}\"" )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
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

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")

    testImplementation("org.powermock:powermock-classloading-xstream:2.0.9")// Pour mocker les classes finales et statiques

    // Pour les ViewModels et autres composants Android
    testImplementation("androidx.arch.core:core-testing:2.2.0")


    // FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)

    //GOOGLE MAPS
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)


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

    implementation(libs.places)

    implementation(libs.okhttp)
    implementation(libs.gson)

    //Dagger
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.dagger.android.processor)

    //Glide
    implementation(libs.github.glide)
    annotationProcessor(libs.glide.compiler)

    //Retrofit
    implementation(libs.retrofit)
    implementation (libs.converter.gson)

    implementation(libs.fragment.ktx)

    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.guava:guava:31.0.1-android")

}

configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.13.0")
        force("androidx.media:media:1.0.0")
    }
}
