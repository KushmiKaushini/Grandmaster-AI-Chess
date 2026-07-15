# Retrofit, OkHttp, Moshi, and API Models Proguard Config

# Retain generic signatures and annotations for reflection/serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, Annotation, *Annotation*, SourceFile, LineNumberTable

# Preserve Moshi classes and generated adapters
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }
-dontwarn com.squareup.moshi.**

# Keep our network API data models
-keep class com.example.network.** { *; }
-keep class com.example.model.** { *; }
-keep class com.example.ui.AchievementState { *; }
-keep class com.example.ui.ChessState { *; }

# Retrofit specific rules
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}

# OkHttp specific rules
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Kotlin reflect and coroutines metadata active safely
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.**
