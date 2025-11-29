# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ================================================================================================
# CRASH PREVENTION RULES - CRITICAL
# ================================================================================================
# These rules prevent NoSuchMethodError crashes by ensuring all repository methods are preserved
# Reference: Crash in v6.2.0 - cacheNotesOnView method missing at runtime

# Keep all repository classes and their methods (including suspend functions)
-keep class com.dolphin.jetpack.data.repository.** { *; }
-keep class com.dolphin.jetpack.domain.repository.** { *; }

# Keep all ViewModel classes and their methods (including suspend functions)
-keep class com.dolphin.jetpack.presentation.viewmodel.** { *; }

# Keep all data models used with Retrofit/Room/Serialization
-keep class com.dolphin.jetpack.data.model.** { *; }
-keep class com.dolphin.jetpack.domain.model.** { *; }

# Keep all API response models for Gson/Retrofit deserialization
-keep class com.dolphin.jetpack.data.remote.** { *; }

# Keep all local database entities
-keep class com.dolphin.jetpack.data.local.entity.** { *; }

# ================================================================================================
# KOTLIN & COROUTINES
# ================================================================================================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.dolphin.jetpack.**$$serializer { *; }
-keepclassmembers class com.dolphin.jetpack.** {
    *** Companion;
}
-keepclasseswithmembers class com.dolphin.jetpack.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ================================================================================================
# RETROFIT & NETWORKING
# ================================================================================================
# Retrofit
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ================================================================================================
# ROOM DATABASE
# ================================================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAO methods
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract ** *Dao();
}

# ================================================================================================
# FIREBASE
# ================================================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ================================================================================================
# COMPOSE
# ================================================================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ================================================================================================
# ADMOB
# ================================================================================================
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ================================================================================================
# GENERAL ANDROID
# ================================================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ================================================================================================
# DEBUGGING & OPTIMIZATION
# ================================================================================================
# Preserve line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release builds (optional - uncomment if needed)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }