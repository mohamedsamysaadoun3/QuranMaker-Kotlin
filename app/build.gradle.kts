plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "hazem.nurmontage.videoquran"
    compileSdk = 35

    defaultConfig {
        applicationId = "hazem.nurmontage.videoquran"
        minSdk = 24
        targetSdk = 35
        versionCode = 21000200
        versionName = "6.7.1-QuranMaker"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // ═══════════════════════════════════════════════════════════
    // إصدارات مستخرجة من الهندسة العكسية للـ APK الأصلي
    // جميع الأرقام متطابقة مع الملفات المكتشفة
    // ═══════════════════════════════════════════════════════════

    // ─── AndroidX Core ───
    // مكتشف: compileSdkVersion=35, targetSdk=35, minSdk=24
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.annotation:annotation:1.9.1")

    // ─── AndroidX Lifecycle ───
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // ─── Material Design 3 ───
    // مكتشف: com.google.android.material في jadx_output
    implementation("com.google.android.material:material:1.12.0")

    // ─── Splash Screen ───
    // مكتشف: androidx.core.splashscreen في jadx_output
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ─── Emoji2 ───
    // مكتشف: androidx.emoji2 في jadx_output
    implementation("androidx.emoji2:emoji2:1.4.0")

    // ─── Profile Installer ───
    // مكتشف: androidx.profileinstaller في jadx_output
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // ─── Window ───
    // مكتشف: uses-library androidx.window في AndroidManifest
    implementation("androidx.window:window:1.3.0")

    // ═══════════════════════════════════════════════════════════
    // محركات الميديا - Media Engines (CRITICAL)
    // ═══════════════════════════════════════════════════════════

    // ─── FFmpegKit ───
    // مكتشف: FFmpeg version "6.0" من NativeLoader.smali
    // مكتشف: 10 مكتبات .so أصلية (libavcodec 13MB, libavfilter 3.6MB, etc.)
    // مكتشف: مكتبات خارجية مدعومة: dav1d, fontconfig, freetype, fribidi,
    //   gmp, gnutls, kvazaar, mp3lame, libass, iconv, libilbc, libtheora,
    //   libvidstab, libvorbis, libvpx, libwebp, libxml2, opencore-amr,
    //   openh264, openssl, opus, rubberband, sdl2, shine, snappy, soxr,
    //   speex, srt, tesseract, twolame, x264, x265, xvid, zimg
    implementation("com.arthenica:ffmpeg-kit-full:6.0-2")

    // ─── AndroidX Media3 (ExoPlayer) ───
    // مكتشف: androidx.media3.exoplayer + media3.common في jadx_output
    // مكتشف: CmcdData, IcyHeaders imports في EngineActivity
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")

    // ═══════════════════════════════════════════════════════════
    // تحميل ومعالجة الصور - Image Loading
    // ═══════════════════════════════════════════════════════════

    // ─── Glide ───
    // مكتشف: com.bumptech.glide في jadx_output (327 ملف)
    // مكتشف: Glide.with(), DiskCacheStrategy imports في EngineActivity
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ─── Glide Transformations ───
    // مكتشف: jp.wasabeef.glide.transformations (26 ملف)
    // مكتشف: BlurTransformation, CropTransformation, GrayscaleTransformation
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // ═══════════════════════════════════════════════════════════
    // خدمات Google - Google Services
    // ═══════════════════════════════════════════════════════════

    // ─── Google Play Services ───
    // مكتشف: google_play_services_version = 12451000 من integers.xml
    // مكتشف: play-services-base 18.5.0, play-services-basement 18.4.0
    // مكتشف: play-services-tasks 18.2.0 من .properties files
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-tasks:18.2.0")

    // ─── Google Play Billing ───
    // مكتشف: com.google.android.gms.internal.play_billing في jadx_output
    // مكتشف: SupportBillingActivity, BillingPreferences, ProVersionActivity
    implementation("com.google.android.gms:play-services-billing:7.1.1")

    // ─── Google AdMob ───
    // مكتشف: AdsTuffahActivity في AndroidManifest
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // ─── Firebase ───
    // مكتشف: firebase-encoders 17.0.0, firebase-encoders-json 18.0.0
    // مكتشف: transport-runtime 3.1.8, transport-backend-cct 3.1.8
    implementation("com.google.firebase:firebase-analytics:22.1.2")
    implementation("com.google.firebase:firebase-analytics-ktx:22.1.2")

    // ═══════════════════════════════════════════════════════════
    // مؤثرات بصرية - Visual Effects
    // ═══════════════════════════════════════════════════════════

    // ─── Konfetti ───
    // مكتشف: nl.dionsegijn.konfetti في jadx_output (30 ملف)
    // مكتشف: Kotlin metadata: mv={1, 8, 0}, xi=48
    implementation("nl.dionsegijn:konfetti:2.0.4")

    // ═══════════════════════════════════════════════════════════
    // أدوات مساعدة - Utilities
    // ═══════════════════════════════════════════════════════════

    // ─── Apache Commons IO ───
    // مكتشف: org.apache.commons.io في jadx_output
    // مكتشف: FileUtils import في ProgressViewActivity
    implementation("commons-io:commons-io:2.16.1")

    // ─── PairIP Protection ───
    // مكتشف: com.pairip في jadx_output (9 ملف)
    implementation("com.pairip:pairip-android:3.3.5")

    // ─── Kotlin Standard Library ───
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ═══════════════════════════════════════════════════════════
    // Testing
    // ═══════════════════════════════════════════════════════════
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
