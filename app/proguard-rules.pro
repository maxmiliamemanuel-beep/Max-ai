# MKUU AI ProGuard Rules

# Kotlin
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-dontwarn kotlin.**

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.mkuuai.android.data.model.** { *; }
-keep class com.mkuuai.android.data.api.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Timber
-dontwarn timber.log.Timber

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }
