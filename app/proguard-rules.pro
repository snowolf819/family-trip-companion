# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Data classes
-keep class com.familytrip.companion.data.model.** { *; }
